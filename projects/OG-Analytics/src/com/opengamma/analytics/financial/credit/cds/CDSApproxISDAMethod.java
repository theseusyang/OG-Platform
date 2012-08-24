/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.SingleRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixty;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
/**
 * An approximation to the calculation method for the ISDA CDS model
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class CDSApproxISDAMethod {
  
  private static DayCount s_act365 = new ActualThreeSixtyFive();
  
  private static final double PRICING_TIME = 0.0;
  private static final double ONE_DAY_ACT_365F = 1.0 / 365.0;
  private static final double HALF_DAY_ACT_365F = 0.5 / 365.0;
  
  private static class Timeline {
    public final double[] timePoints;
    public final double[] discountFactors;
    
    public Timeline(final double[] timePoints, final double[] discountFactors) {
      this.timePoints = timePoints;
      this.discountFactors = discountFactors;
    }
  }

  /**
   * Calculate the up-front charge of for a CDS. This is what would change hands if the contract were traded.
   * and is effectively its PV.
   * 
   * Step-in date is the date when ownership of protection is assumed and is normally taken as T+1, so a CDS traded
   * on a particular day becomes effective the next day at 00:01. To value an new CDS contract with start date
   * T >= 0, then step-in date = T+1. To value an existing contract at some point T, step-in date = T+1 again
   * but now start date < 0.
   * 
   * @param cds The CDS to be valued
   * @param discountCurve The discount curve
   * @param hazardRateCurve The credit spread curve
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate the settlement date
   * @param cleanPrice Whether the price is clean (true) or dirty (false)
   * @return PV of the CDS contract
   */
  public double calculateUpfrontCharge(final CDSDerivative cds, final ISDACurve discountCurve, final ISDACurve hazardRateCurve,
    final ZonedDateTime pricingDate, final ZonedDateTime stepinDate, final ZonedDateTime settlementDate, boolean cleanPrice) {
    
    final double settlementTime = getTimeBetween(pricingDate, settlementDate);

    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
    final double offsetPricingTime = -offset;
    final double offsetStepinTime = getTimeBetween(pricingDate, stepinDate) - offset;
    final double offsetMaturityTime = cds.getMaturity() + offset;
    final double protectionStartTime = Math.max(Math.max(cds.getStartTime(), offsetStepinTime), offsetPricingTime);
   
    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    final Timeline accrualTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, cds.getStartTime(), offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, protectionStartTime, cds.getMaturity(), false);
    
    final double settlementDiscountFactor = discountCurve.getDiscountFactor(settlementTime);
    final double stepinDiscountFactor = offsetStepinTime > 0.0 ? discountCurve.getDiscountFactor(offsetStepinTime) : discountCurve.getDiscountFactor(0.0);
    
    return valueCDS(cds, discountCurve, hazardRateCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, cleanPrice);
  }

  public double calculateUpfrontCharge(final CDSDerivative cds, final ISDACurve discountCurve, double flatSpread,
      final ZonedDateTime pricingDate, final ZonedDateTime stepinDate, final ZonedDateTime settlementDate, boolean cleanPrice) {
    
    final double settlementTime = getTimeBetween(pricingDate, settlementDate);

    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
    final double offsetPricingTime = -offset;
    final double offsetStepinTime = getTimeBetween(pricingDate, stepinDate) - offset;
    final double offsetMaturityTime = cds.getMaturity() + offset;
    final double protectionStartTime = Math.max(Math.max(cds.getStartTime(), offsetStepinTime), offsetPricingTime);
   
    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    final Timeline accrualTimeline = buildTimeline(cds, discountCurve, null, cds.getStartTime(), offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, null, protectionStartTime, cds.getMaturity(), false);
    
    final double settlementDiscountFactor = discountCurve.getDiscountFactor(settlementTime);
    final double stepinDiscountFactor = offsetStepinTime > 0.0 ? discountCurve.getDiscountFactor(offsetStepinTime) : discountCurve.getDiscountFactor(0.0);
       
    final double[] timePoints = {cds.getMaturity()};
    final double[] dataPoints = {flatSpread};

    final ISDACDSCoupon[] premiums = cds.getPremium().getPayments();
    final ZonedDateTime startDate = premiums[0].getAccrualStartDate();
    final ZonedDateTime maturityDate = premiums[premiums.length - 1].getAccrualEndDate();
    final CDSDefinition zeroCDSDefinition = makeZeroCDSDefintion(startDate, maturityDate, dataPoints[0], cds.getRecoveryRate());
    final CDSDerivative zeroCDS = zeroCDSDefinition.toDerivative(pricingDate, "IR_CURVE", "TEMP_CURVE");

    SingleRootFinder<Double, Double> rootFinder = new BrentSingleRootFinder(1E-17);
    
    //double guess = dataPoints[0] / (1 + cds.getRecoveryRate());
    
    dataPoints[0] = rootFinder.getRoot(
      new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double x) {
          dataPoints[0] = x;   
          ISDACurve tempCurve = new ISDACurve("TEMP_CURVE", timePoints, dataPoints, 0.0);
          return valueCDS(zeroCDS, discountCurve, tempCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, true);
        }  
      },
      0.0, 100.0
    );
    
    final ISDACurve hazardRateCurve = new ISDACurve("HAZARD_RATE_CURVE", timePoints, dataPoints, 0.0);
      
    return valueCDS(cds, discountCurve, hazardRateCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, cleanPrice);
  }
  
  private CDSDefinition makeZeroCDSDefintion(ZonedDateTime startDate, ZonedDateTime maturity, final double spread, final double recoveryRate) {
    
    final double notional = 1.0;
    
    // TODO: source these values from somewhere
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final DayCount dayCount = new ActualThreeSixty();
    final BusinessDayConvention convention = new FollowingBusinessDayConvention();
    
    final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(
        Currency.USD, startDate, maturity,
        couponFrequency, calendar, dayCount, convention,
        notional, spread,
        /* protect start */ true);
    
    return new CDSDefinition(premiumDefinition, null, startDate, maturity, notional, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, dayCount);
  }
  
  private double valueCDS(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline paymentTimeline, Timeline accrualTimeline, Timeline contingentTimeline,
  final double stepinTime, final double stepinDiscountFactor, final double settlementDiscountFactor, boolean cleanPrice) {
    
    if (stepinTime < PRICING_TIME) {
      throw new OpenGammaRuntimeException("Cannot value a CDS with step-in date before pricing date");
    }
    
    final double contingentLeg = valueContingentLeg(cds, contingentTimeline, hazardRateCurve, settlementDiscountFactor);
    final double feeLeg = valueFeeLeg(cds, paymentTimeline, accrualTimeline, hazardRateCurve, stepinTime, stepinDiscountFactor, settlementDiscountFactor);
    final double dirtyPrice = (contingentLeg - feeLeg) * cds.getNotional();
    
    return cleanPrice ? dirtyPrice + cds.getAccruedInterest() : dirtyPrice;
  }

  /**
   * Value the premium leg of a CDS taken from the step-in date.
   * @param cds The CDS contract being priced
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param stepinDate The step-in date
   * @return PV of the CDS premium leg
   */
  private double valueFeeLeg(final CDSDerivative cds, final Timeline paymentTimeline, final Timeline accrualTimeline, final ISDACurve hazardRateCurve,
    final double stepinTime, final double stepinDiscountFactor, final double settlementDiscountFactor) {
    
    // If the "protect start" flag is set, then the start date of the CDS is protected and observations are made at the start
    // of the day rather than the end. This is modelled by shifting all period start/end dates one day forward,
    // and adding one extra day's protection to the final period
    
    final ISDACDSCoupon[] coupons = cds.getPremium().getPayments();
    final int maturityIndex = coupons.length - 1;
    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
 
    ISDACDSCoupon payment;
    double periodEndTime;
    double amount, survival, discount;
    double result = 0.0;
    
    int startIndex, endIndex = 0;
    
    for (int i = 0; i < coupons.length; i++) {
      
      payment = coupons[i];
      periodEndTime = payment.getAccrualEndTime() - (i < maturityIndex ? offset : 0.0);
      
      amount = payment.getFixedRate() * payment.getPaymentYearFraction();
      survival = hazardRateCurve.getDiscountFactor(periodEndTime);
      discount = paymentTimeline.discountFactors[i];
      result += amount * survival * discount;
      
      if (cds.isAccrualOnDefault()) {
        
        startIndex = endIndex;
        while (accrualTimeline.timePoints[endIndex] < periodEndTime) { ++endIndex; }
          
        result += valueFeeLegAccrualOnDefault(amount, accrualTimeline, hazardRateCurve, startIndex, endIndex, stepinTime, stepinDiscountFactor);
      }
    }
    
    return result / settlementDiscountFactor;
  }

  /**
   * Calculate the accrual-on-default portion of the PV for a specified accrual period.
   * 
   * @param amount Amount of premium that would be accrued over the entire period (in the case of no default)
   * @param timeline The accrual time line, covers the entire CDS (excluding past accrual periods), not just the accrual period being evaluated 
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param startIndex Index in to the time line for the start of the current accrual period
   * @param endIndex Index in to the time line for the end of the current accrual period
   * @param stepinTime Step-in time for the CDS contract
   * @param stepinDiscountFactor Associated discount factor for the step-in time
   * @return Accrual-on-default portion of PV for the accrual period
   */
  private double valueFeeLegAccrualOnDefault(final double amount, final Timeline timeline, final ISDACurve hazardRateCurve, final int startIndex,
    final int endIndex, final double stepinTime, final double stepinDiscountFactor) {
    
    final double startTime = timeline.timePoints[startIndex];
    final double endTime = timeline.timePoints[endIndex];
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    final double accrualRate = amount / (endTime - startTime); // TODO: Handle startTime == endTime

    double t0, t1, dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;
    
    t0 = subStartTime - startTime + HALF_DAY_ACT_365F;
    survival0 = hazardRateCurve.getDiscountFactor(subStartTime);
    discount0 = startTime < stepinTime || startTime < PRICING_TIME ? stepinDiscountFactor : timeline.discountFactors[startIndex];
    
    value = 0.0;
    
    for (int i = startIndex + 1; i <= endIndex; ++i) { 
      
      if (timeline.timePoints[i] <= stepinTime) {
        continue;
      }

      t1 = timeline.timePoints[i] - startTime + HALF_DAY_ACT_365F;
      dt = t1 - t0;
      
      survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[i]);
      discount1 = timeline.discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * survival0 * discount0
        * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

      value += valueForTimeStep;

      t0 = t1;
      survival0 = survival1;
      discount0 = discount1;
    }

    return value;
  }

  /**
   * Value the contingent leg of a CDS taken from the step-in date.
   * 
   * @param cds The derivative object representing the CDS
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV of the CDS default leg
   */
  private double valueContingentLeg(final CDSDerivative cds, final Timeline contingentTimeline, final ISDACurve hazardRateCurve, final double settlementDiscountFactor) {

    final double recoveryRate = cds.getRecoveryRate();
    final double value = cds.isPayOnDefault()
      ? valueContingentLegPayOnDefault(recoveryRate, contingentTimeline, hazardRateCurve)
      : valueContingentLegPayOnMaturity(recoveryRate, contingentTimeline, hazardRateCurve);

    return value / settlementDiscountFactor;
  }

  /**
   * Value the default leg, assuming any possible payout is received at the time of default.
   * 
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param timeline The contingent leg time line, bounded by startTime and maturity
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnDefault(final double recoveryRate, final Timeline timeline, final ISDACurve hazardRateCurve) {
    
    final double maturity = timeline.timePoints[timeline.timePoints.length - 1];
    final double loss = 1.0 - recoveryRate;
    
    if (maturity < PRICING_TIME) {
      return 0.0;
    }
    
    double dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, valueForTimeStep, value;
    
    survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[0]);
    discount1 = timeline.timePoints[0] > PRICING_TIME ? timeline.discountFactors[0] : 1.0;
    value = 0.0;

    for (int i = 1; i < timeline.timePoints.length; ++i) {
      
      dt = timeline.timePoints[i] - timeline.timePoints[i - 1];

      survival0 = survival1;
      discount0 = discount1;
      survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[i]);
      discount1 = timeline.discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * survival0 * discount0;

      value += valueForTimeStep;
    }

    return value;
  }
  
  /**
   * Value the default leg, assuming any possible payout is received at maturity.
   * 
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param timeline The contingent leg time line, bounded by startTime and maturity
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnMaturity(final double recoveryRate, final Timeline timeline, final ISDACurve hazardRateCurve) {
    
    final int maturityIndex = timeline.timePoints.length - 1; 
    
    if (timeline.timePoints[maturityIndex] < PRICING_TIME) {
      return 0.0;
    }
    
    final double loss = 1.0 - recoveryRate;
    final double survival0 = hazardRateCurve.getDiscountFactor(timeline.timePoints[0]);
    final double survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[maturityIndex]);
    final double discount = timeline.discountFactors[maturityIndex];
    
    return (survival0 - survival1) * discount * loss;
  }
  
  
  private Timeline buildTimeline(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, double startTime, double endTime, boolean includeSchedule) {
    
    NavigableSet<Double> allTimePoints = new TreeSet<Double>();
    
    final double[] discountCurveTimePoints = discountCurve.getTimePoints();
    for (int i = 0; i < discountCurveTimePoints.length; ++i) {
      allTimePoints.add(new Double(discountCurveTimePoints[i]));
    }
    
    if (hazardRateCurve != null) {
      final double[] hazardRateCurveTimePoints = hazardRateCurve.getTimePoints();
      for (int i = 0; i < hazardRateCurveTimePoints.length; ++i) {
        allTimePoints.add(new Double(hazardRateCurveTimePoints[i]));
      }
    } else {
      allTimePoints.add(new Double(cds.getMaturity()));
    }
    
    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));
    
    Set<Double> timePointsInRange;
    
    if (includeSchedule) {
      
      final ISDACDSCoupon[] premiums = cds.getPremium().getPayments();
      final int maturityIndex = premiums.length - 1;
      final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
      
      final double offsetStartTime = premiums[0].getAccrualStartTime() - offset;
      allTimePoints.add(new Double(offsetStartTime));
    
      double periodEndTime;
      
      for (int i = 0; i < premiums.length; i++) {
        
        periodEndTime = premiums[i].getAccrualEndTime() - (i < maturityIndex ? offset : 0.0);
        allTimePoints.add(new Double(periodEndTime));
      }
      
      timePointsInRange = allTimePoints.subSet(new Double(offsetStartTime), true, new Double(endTime), true);
      
    } else {
      
      timePointsInRange = allTimePoints.subSet(new Double(startTime), true, new Double(endTime), true);
    }
    
    Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);
    
    double[] timePoints = new double[boxed.length];
    double[] discountFactors = new double[boxed.length];
    
    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
      discountFactors[i] = discountCurve.getDiscountFactor(timePoints[i]);
    }
    
    return new Timeline(timePoints, discountFactors);
  }
  
  private Timeline buildPaymentTimeline(CDSDerivative cds, ISDACurve discountCurve) {
    
    final ISDACDSCoupon[] payments = cds.getPremium().getPayments();
    
    double[] timePoints = new double[payments.length];
    double[] discountFactors = new double[payments.length];
    
    for (int i = 0; i < payments.length; ++i) {
      timePoints[i] = payments[i].getPaymentTime();
      discountFactors[i] = discountCurve.getDiscountFactor(timePoints[i]);
    }
    
    return new Timeline(timePoints, discountFactors);
  }
  
  /**
   * Return the fraction of a year between two dates according the the ACT365 convention.
   * If date2 < date1 the result will be negative.
   * 
   * @param date1 The first date
   * @param date2 The second date
   * @return Real time value in years
   */
  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {

    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());
    final boolean timeIsNegative = date1.isAfter(rebasedDate2);

    if (!timeIsNegative) {
      return s_act365.getDayCountFraction(date1, rebasedDate2);
    } else {
      return -1.0 * s_act365.getDayCountFraction(rebasedDate2, date1);
    }
  }

  public CurrencyAmount presentValue(CDSDerivative cdsDerivative, YieldCurveBundle curveBundle) {
    return null;
  }
}
