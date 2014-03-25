/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AbstractAnnuityDefinitionBuilder.CouponStub;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.EndOfMonthRollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapNotionalVisitor;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.lookup.irs.InterestRateSwapNotionalAmountVisitor;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Convert swap and trade to analytics type. If the trade holds fee information fee cashflows are incorporated into the resulting annuities.
 */
public class InterestRateSwapSecurityTradeConverter implements FinancialSecurityVisitorWithData<Trade, InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;
  /** A convention source */
  private final ConventionSource _conventionSource;

  /**
   * @param holidaySource The holiday source, not <code>null</code>
   * @param conventionSource The convention source, not <code>null</code>
   */
  public InterestRateSwapSecurityTradeConverter(final HolidaySource holidaySource, final ConventionSource conventionSource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  @Override
  public InstrumentDefinition<?> visitInterestRateSwapSecurity(InterestRateSwapSecurity security, Trade trade) {
    ArgumentChecker.notNull(security, "swap security");

    LocalDate startDate = security.getEffectiveDate();
    LocalDate endDate = security.getUnadjustedMaturityDate();

    final List<AnnuityDefinition<?>> annuities = Lists.newArrayListWithExpectedSize(security.getLegs().size());
    for (final InterestRateSwapLeg leg : security.getLegs()) {
      AnnuityDefinition<?> legAnnuity = getAnnuityDefinition(leg.getPayReceiveType() == PayReceiveType.PAY, startDate, endDate, security.getNotionalExchange(), leg);
      annuities.add(legAnnuity);
    }
    if (trade != null) {
      AnnuityDefinition<?> feeLeg = buildFeeAnnuityDefinition(trade);
      if (feeLeg != null) {
        annuities.add(feeLeg);
      }
    }
    return new SwapMultilegDefinition(annuities.toArray(new AnnuityDefinition<?>[annuities.size()]));
  }

  private AnnuityDefinition<?> getAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    if (leg instanceof FixedInterestRateSwapLeg) {
      return buildFixedAnnuityDefinition(payer, startDate, endDate, notionalExchange, leg);
    } else if (leg instanceof FloatingInterestRateSwapLeg) {
      return buildFloatingAnnuityDefinition(payer, startDate, endDate, notionalExchange, leg);
    } else {
      throw new OpenGammaRuntimeException("Unsupported leg type");
    }
  }

  private AnnuityDefinition<?> buildFloatingAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    FloatingInterestRateSwapLeg floatLeg = (FloatingInterestRateSwapLeg) leg;

    AdjustedDateParameters maturityDateParameters = null;
    if (leg.getMaturityDateCalendars() != null && leg.getMaturityDateBusinessDayConvention() != null) {
      Calendar maturityDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getMaturityDateCalendars().toArray(new ExternalId[leg.getMaturityDateCalendars().size()]));
      maturityDateParameters = new AdjustedDateParameters(maturityDateCalendar, leg.getMaturityDateBusinessDayConvention());
    }

    AdjustedDateParameters accrualPeriodParameters = null;
    if (leg.getAccrualPeriodCalendars() != null && leg.getAccrualPeriodBusinessDayConvention() != null) {
      Calendar accrualPeriodCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()]));
      accrualPeriodParameters = new AdjustedDateParameters(accrualPeriodCalendar, leg.getAccrualPeriodBusinessDayConvention());
    }

    RollDateAdjuster rollDateAdjuster = null;
    if (leg.getRollConvention() == RollConvention.EOM) {
      rollDateAdjuster = EndOfMonthRollDateAdjuster.getAdjuster();
    } else {
      rollDateAdjuster = leg.getRollConvention().getRollDateAdjuster(0);
    }

    AdjustedDateParameters resetDateParameters = null;
    if (floatLeg.getResetPeriodCalendars() != null && floatLeg.getResetPeriodBusinessDayConvention() != null) {
      Calendar resetCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatLeg.getResetPeriodCalendars().toArray(new ExternalId[floatLeg.getResetPeriodCalendars().size()]));
      resetDateParameters = new AdjustedDateParameters(resetCalendar, floatLeg.getResetPeriodBusinessDayConvention());
    }

    double spread = Double.NaN;
    if (floatLeg.getSpreadSchedule() != null) {
      spread = floatLeg.getSpreadSchedule().getInitialRate();
    }

    final int paymentOffset = floatLeg.getPaymentOffset();

    final IndexDeposit index;
    if (FloatingRateType.IBOR == floatLeg.getFloatingRateType()) {
      index = new IborIndex(
          leg.getNotional().getCurrency(),
          getTenor(floatLeg.getResetPeriodFrequency()),
          -floatLeg.getFixingDateOffset(), // spot lag
          leg.getDayCountConvention(),
          leg.getPaymentDateBusinessDayConvention(),
          RollConvention.EOM == leg.getRollConvention(),
          floatLeg.getFloatingReferenceRateId().getValue());

    } else if (FloatingRateType.OIS == floatLeg.getFloatingRateType()) {
      Convention convention = _conventionSource.getSingle(floatLeg.getFloatingReferenceRateId());
      if (convention == null) {
        throw new OpenGammaRuntimeException("Convention not found for " + floatLeg.getFloatingReferenceRateId());
      }
      if (!(convention instanceof OvernightIndexConvention)) {
        throw new OpenGammaRuntimeException("Mis-match between floating rate type " + floatLeg.getFloatingRateType() + " and convention " + convention.getClass());
      }
      OvernightIndexConvention onIndexConvention = (OvernightIndexConvention) convention;
      index = new IndexON(
          floatLeg.getFloatingReferenceRateId().getValue(),
          leg.getNotional().getCurrency(),
          floatLeg.getDayCountConvention(),
          onIndexConvention.getPublicationLag());

    } else {
      throw new OpenGammaRuntimeException("Unsupported floating rate type " + floatLeg.getFloatingRateType());
    }

    OffsetAdjustedDateParameters paymentDateParameters = null;
    if (leg.getPaymentDateCalendars() != null && leg.getPaymentDateBusinessDayConvention() != null) {
      Calendar paymentDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getPaymentDateCalendars().toArray(new ExternalId[leg.getPaymentDateCalendars().size()]));
      paymentDateParameters = new OffsetAdjustedDateParameters(paymentOffset, OffsetType.BUSINESS, paymentDateCalendar, leg.getPaymentDateBusinessDayConvention());
    }

    OffsetAdjustedDateParameters fixingDateParameters = null;
    if (floatLeg.getFixingDateCalendars() != null && floatLeg.getFixingDateBusinessDayConvention() != null) {
      Calendar fixingDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatLeg.getFixingDateCalendars().toArray(new ExternalId[floatLeg.getFixingDateCalendars().size()]));
      fixingDateParameters = new OffsetAdjustedDateParameters(floatLeg.getFixingDateOffset(), floatLeg.getFixingDateOffsetType(), fixingDateCalendar, floatLeg.getFixingDateBusinessDayConvention());
    }

    com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod compoundingMethod;
    switch (floatLeg.getCompoundingMethod()) {
      case FLAT:
        compoundingMethod = com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod.FLAT;
        break;
      case STRAIGHT:
        compoundingMethod = com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod.STRAIGHT;
        break;
      case NONE:
        compoundingMethod = null;
        break;
      default:
        throw new OpenGammaRuntimeException("Unsupported compounding method");
    }

    Pair<CouponStub, CouponStub> stubs = parseStubs(leg.getStubCalculationMethod());
    CouponStub startStub = stubs.getFirst();
    CouponStub endStub = stubs.getSecond();

    List<Double> notionalList = leg.getNotional().getNotionals();
    double[] notionalSchedule;
    if (notionalList.isEmpty()) {
      notionalSchedule = new double[] {(payer ? -1 : 1) * leg.getNotional().getInitialAmount()};
    } else {
      notionalSchedule = new double[notionalList.size()];
      for (int i = 0; i < notionalSchedule.length; i++) {
        notionalSchedule[i] = (payer ? -1 : 1) * notionalList.get(i);
      }
    }

    return new FloatingAnnuityDefinitionBuilder().
        payer(payer).
        currency(leg.getNotional().getCurrency()).
        notional(getNotionalProvider(leg.getNotional(), leg.getAccrualPeriodBusinessDayConvention(),
            new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()])))).
        startDate(startDate).
        endDate(endDate).
        endDateAdjustmentParameters(maturityDateParameters).
        dayCount(leg.getDayCountConvention()).
        rollDateAdjuster(rollDateAdjuster).
        exchangeInitialNotional(notionalExchange.isExchangeInitialNotional()).
        exchangeFinalNotional(notionalExchange.isExchangeFinalNotional()).
        accrualPeriodFrequency(getTenor(leg.getPaymentDateFrequency())).
        accrualPeriodParameters(accrualPeriodParameters).
        paymentDateRelativeTo(leg.getPaymentDateRelativeTo()).
        paymentDateAdjustmentParameters(paymentDateParameters).
        spread(spread).
        index(index).
        resetDateAdjustmentParameters(resetDateParameters).
        resetRelativeTo(floatLeg.getResetDateRelativeTo()).
        fixingDateAdjustmentParameters(fixingDateParameters).
        compoundingMethod(compoundingMethod).
        startStub(startStub).
        endStub(endStub).
        initialRate(floatLeg.getCustomRates() != null ? floatLeg.getCustomRates().getInitialRate() : Double.NaN).
        build();
  }

  private AnnuityDefinition<?> buildFixedAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) leg;

    AdjustedDateParameters maturityDateParameters = null;
    if (leg.getMaturityDateCalendars() != null && leg.getMaturityDateBusinessDayConvention() != null) {
      Calendar maturityDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getMaturityDateCalendars().toArray(new ExternalId[leg.getMaturityDateCalendars().size()]));
      maturityDateParameters = new AdjustedDateParameters(maturityDateCalendar, leg.getMaturityDateBusinessDayConvention());
    }

    AdjustedDateParameters accrualPeriodParameters = null;
    if (leg.getAccrualPeriodCalendars() != null && leg.getAccrualPeriodBusinessDayConvention() != null) {
      Calendar accrualPeriodCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()]));
      accrualPeriodParameters = new AdjustedDateParameters(accrualPeriodCalendar, leg.getAccrualPeriodBusinessDayConvention());
    }

    OffsetAdjustedDateParameters paymentDateParameters = null;
    if (leg.getPaymentDateCalendars() != null && leg.getPaymentDateBusinessDayConvention() != null) {
      Calendar paymentDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getPaymentDateCalendars().toArray(new ExternalId[leg.getPaymentDateCalendars().size()]));
      paymentDateParameters = new OffsetAdjustedDateParameters(
          leg.getPaymentOffset(),
          OffsetType.BUSINESS,
          paymentDateCalendar,
          leg.getPaymentDateBusinessDayConvention());
    }

    RollDateAdjuster rollDateAdjuster = null;
    if (leg.getRollConvention() == RollConvention.EOM) {
      rollDateAdjuster = EndOfMonthRollDateAdjuster.getAdjuster();
    } else {
      rollDateAdjuster = leg.getRollConvention().getRollDateAdjuster(0);
    }

    Pair<CouponStub, CouponStub> stubs = parseStubs(leg.getStubCalculationMethod());
    CouponStub startStub = stubs.getFirst();
    CouponStub endStub = stubs.getSecond();

    List<Double> notionalList = leg.getNotional().getNotionals();
    double[] notionalSchedule;
    if (notionalList.isEmpty()) {
      notionalSchedule = new double[] {(payer ? -1 : 1) * leg.getNotional().getInitialAmount()};
    } else {
      notionalSchedule = new double[notionalList.size()];
      for (int i = 0; i < notionalSchedule.length; i++) {
        notionalSchedule[i] = (payer ? -1 : 1) * notionalList.get(i);
      }
    }

    return new FixedAnnuityDefinitionBuilder().
        payer(payer).
        currency(leg.getNotional().getCurrency()).
//        notional((payer ? -1 : 1) * leg.getNotional().getAmount()).
        notional(getNotionalProvider(leg.getNotional(), leg.getAccrualPeriodBusinessDayConvention(),
            new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()])))).
        startDate(startDate).
        endDate(endDate).
        endDateAdjustmentParameters(maturityDateParameters).
        dayCount(leg.getDayCountConvention()).
        rollDateAdjuster(rollDateAdjuster).
        exchangeInitialNotional(notionalExchange.isExchangeInitialNotional()).
        exchangeFinalNotional(notionalExchange.isExchangeFinalNotional()).
        accrualPeriodFrequency(getTenor(leg.getPaymentDateFrequency())).
        accrualPeriodParameters(accrualPeriodParameters).
        paymentDateRelativeTo(leg.getPaymentDateRelativeTo()).
        paymentDateAdjustmentParameters(paymentDateParameters).
        rate(fixedLeg.getRate().getInitialRate()).
        startStub(startStub).
        endStub(endStub).
        build();
  }

  /**
   * Build annuity from trade fee details.
   * Multiple fee's are accepted, stored as attributes on the trade.
   * FEE_1_DATE, the date in ISO-8601 format, e.g. "2011-12-31"
   * FEE_1_CURRENCY, the 3-letter currency code. Must equal the security currency.
   * FEE_1_AMOUNT, the positive amount.
   * FEE_1_DIRECTION, PAY or RECEIVE depending on payment direction.
   *
   * Any number of fees are permitted, they must be labelled as FEE_1..., FEE_2..., FEE_3... etc.
   * with no gaps in the sequence.
   *
   * @param trade the trade.
   * @return fee annuity
   */
  private AnnuityDefinition<?> buildFeeAnnuityDefinition(final Trade trade) {
    List<CouponFixedDefinition> fees = new ArrayList<>();
    Security security = trade.getSecurity();
    Currency securityCurrency = FinancialSecurityUtils.getCurrency(security);
    for (int i = 1;; i++) {
      if (!trade.getAttributes().containsKey(String.format("FEE_%d_DATE", i))) {
        break;
      }
      final LocalDate feeDate = LocalDate.parse(trade.getAttributes().get(String.format("FEE_%d_DATE", i)));
      final ZonedDateTime feeTime = feeDate.atStartOfDay(ZoneId.systemDefault());
      final Currency ccy = Currency.of(trade.getAttributes().get(String.format("FEE_%d_CURRENCY", i)));
      ArgumentChecker.isTrue(securityCurrency.equals(ccy), "Fee must be in security currency {} got {}", securityCurrency, ccy);
      final Double amount = Double.parseDouble(trade.getAttributes().get(String.format("FEE_%d_AMOUNT", i)));
      final PayReceiveType payOrReceive = PayReceiveType.valueOf(trade.getAttributes().get(String.format("FEE_%d_DIRECTION", i)));
      final CouponFixedDefinition payment = new CouponFixedDefinition(ccy, feeTime, feeTime, feeTime, 1, payOrReceive == PayReceiveType.PAY ? -amount : amount, 1);
      fees.add(payment);
    }
    if (!fees.isEmpty()) {
      return new AnnuityDefinition<>(fees.toArray(new CouponFixedDefinition[fees.size()]), new MondayToFridayCalendar(""));
    } else {
      return null;
    }
  }

  /**
   * Converts the StubCalculationMethod to CouponStubs.
   */
  private Pair<CouponStub, CouponStub> parseStubs(final StubCalculationMethod stubCalcMethod) {
    CouponStub startStub = null;
    CouponStub endStub = null;

    if (stubCalcMethod != null) {
      stubCalcMethod.validate();
      StubType stubType = stubCalcMethod.getType();

      // first stub
      double firstStubRate = stubCalcMethod.hasFirstStubRate() ? stubCalcMethod.getFirstStubRate() : Double.NaN;
      LocalDate firstStubDate = stubCalcMethod.getFirstStubEndDate();
      Tenor firstStubStartIndex = stubCalcMethod.getFirstStubStartIndex();
      Tenor firstStubEndIndex = stubCalcMethod.getFirstStubEndIndex();

      // last stub
      double finalStubRate = stubCalcMethod.hasLastStubRate() ? stubCalcMethod.getLastStubRate() : Double.NaN;
      LocalDate finalStubDate = stubCalcMethod.getLastStubEndDate();
      Tenor lastStubStartIndex = stubCalcMethod.getLastStubStartIndex();
      Tenor lastStubEndIndex = stubCalcMethod.getLastStubEndIndex();

      if (StubType.BOTH == stubType) {
        if (!Double.isNaN(firstStubRate)) {
          startStub = new CouponStub(stubType, firstStubDate, stubCalcMethod.getFirstStubRate());
        } else if (firstStubStartIndex != null && firstStubEndIndex != null) {
          startStub = new CouponStub(stubType, firstStubDate, firstStubStartIndex.getPeriod(), firstStubEndIndex.getPeriod());
        } else {
          startStub = new CouponStub(stubType, firstStubDate);
        }

        if (!Double.isNaN(finalStubRate)) {
          endStub = new CouponStub(stubType, finalStubDate, stubCalcMethod.getLastStubRate());
        } else if (lastStubStartIndex != null && lastStubEndIndex != null) {
          endStub = new CouponStub(stubType, finalStubDate, lastStubStartIndex.getPeriod(), lastStubEndIndex.getPeriod());
        } else {
          endStub = new CouponStub(stubType, finalStubDate);
        }

      } else if (StubType.LONG_START == stubType || StubType.SHORT_START == stubType) {
        if (!Double.isNaN(firstStubRate)) {
          startStub = new CouponStub(stubType, firstStubDate, firstStubRate);
        } else if (firstStubStartIndex != null && firstStubEndIndex != null) {
          startStub = new CouponStub(stubType, firstStubStartIndex.getPeriod(), firstStubEndIndex.getPeriod());
        } else {
          startStub = new CouponStub(stubType);
        }
      } else if (StubType.LONG_END == stubType || StubType.SHORT_END == stubType) {
        if (!Double.isNaN(finalStubRate)) {
          endStub = new CouponStub(stubType, finalStubDate, finalStubRate);
        } else if (lastStubStartIndex != null && lastStubEndIndex != null) {
          endStub = new CouponStub(stubType, lastStubStartIndex.getPeriod(), lastStubEndIndex.getPeriod());
        } else {
          endStub = new CouponStub(stubType);
        }
      } else if (stubType != null) {
        startStub = new CouponStub(stubType);
        endStub = new CouponStub(stubType);
      }

    }
    return Pairs.of(startStub, endStub);
  }

  private static Period getTenor(final Frequency freq) {
    if (Frequency.NEVER_NAME.equals(freq.getName())) {
      return Period.ZERO;
    } else if (freq instanceof PeriodFrequency) {
      Period period = ((PeriodFrequency) freq).getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    } else if (freq instanceof SimpleFrequency) {
      Period period =  ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

  //TODO: Would be nice to make this support Notional
  private static NotionalProvider getNotionalProvider(InterestRateSwapNotional notional, BusinessDayConvention convention, Calendar calendar) {
    final InterestRateSwapNotionalVisitor<LocalDate,  Double> visitor = new InterestRateSwapNotionalAmountVisitor();
    final List<LocalDate> dates = notional.getDates();
    if (!dates.isEmpty()) {
      for (int i = 0; i < dates.size(); i++) {
        LocalDate date = dates.remove(i);
        date = convention.adjustDate(calendar, date);
        dates.add(i, date);
      }
      notional = InterestRateSwapNotional.of(notional.getCurrency(), dates, notional.getNotionals(), notional.getShiftTypes());
    }
    final InterestRateSwapNotional adjustednotional = notional;
    return new NotionalProvider() {
      @Override
      public double getAmount(LocalDate date) {
        return adjustednotional.accept(visitor, date);
      }
    };
  }

  @Override
  public InstrumentDefinition<?> visit(FutureSecurity security, Trade data) {
    throw new UnsupportedOperationException("Futures not supported by this visitor.");
  }

  @Override
  public InstrumentDefinition<?> visitForwardRateAgreementSecurity(ForwardRateAgreementSecurity security, Trade data) {
    throw new UnsupportedOperationException("FRA not supported by this visitor.");
  }
}