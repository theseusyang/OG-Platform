/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondFutureOptionTradeConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class BondFutureOptionBlackFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionBlackFunction.class);
  private final String _valueRequirementName;
  private BondFutureOptionTradeConverter _converter;
  private FixedIncomeConverterDataProvider _dataConverter;

  public BondFutureOptionBlackFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _converter = new BondFutureOptionTradeConverter(new BondFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final BondFutureOptionSecurity security = (BondFutureOptionSecurity) trade.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    ;
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency;
    }
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final Object volatilitySurfaceObject = inputs.getValue(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final Object callPriceObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, BondFutureOptionUtils
        .getCallBloombergTicker(security)));
    if (callPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get bond future option call price for " + security.getUniqueId());
    }
    final double callPrice = (Double) callPriceObject;
    final Object putPriceObject = inputs
        .getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, BondFutureOptionUtils.getPutBloombergTicker(security)));
    if (putPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get bond future option put price for " + security.getUniqueId());
    }
    final double putPrice = (Double) putPriceObject;
    final Object futurePriceObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, security.getUnderlyingId()));
    if (futurePriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get bond future price for " + security.getUnderlyingId());
    }
    final double futurePrice = (Double) futurePriceObject;
    final InstrumentDefinition<?> bondFutureOptionDefinition = _converter.convert(trade);
    final BondFutureOptionPremiumTransaction bondFutureOption = (BondFutureOptionPremiumTransaction) _dataConverter.convert(security, bondFutureOptionDefinition, now, fullCurveNames, timeSeries);
    final ValueProperties properties = getResultProperties(desiredValue, security);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
    final YieldCurveWithBlackCubeBundle data = new YieldCurveWithBlackCubeBundle(getVolatilitySurface(volatilitySurface.getSurface(), callPrice, putPrice, futurePrice, bondFutureOption,
        curves), curves);
    return getResult(bondFutureOption, data, curveCalculationConfig, spec, inputs, desiredValues, security);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof BondFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), getResultProperties(currency)));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String surfaceName = surfaceNames.iterator().next() + "_" + getFutureOptionPrefix(target);
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (!curveCalculationConfig.getTarget().getType().isTargetType(ComputationTargetType.CURRENCY)
        || !currency.equals(ComputationTargetType.CURRENCY.resolve(curveCalculationConfig.getTarget().getUniqueId()))) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource));
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    try {
      final Set<ValueRequirement> tsRequirements = _dataConverter.getConversionTimeSeriesRequirements(target.getTrade().getSecurity(), _converter.convert(target.getTrade()));
      if (tsRequirements == null) {
        return null;
      }
      requirements.addAll(tsRequirements);
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
    final BondFutureOptionSecurity security = (BondFutureOptionSecurity) target.getTrade().getSecurity();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, BondFutureOptionUtils.getCallBloombergTicker(security)));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, BondFutureOptionUtils.getPutBloombergTicker(security)));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, security.getUnderlyingId()));
    return requirements;
  }

  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, MultiCurveCalculationConfig curveCalculationConfig,
      final ValueSpecification spec, final FunctionInputs inputs, final Set<ValueRequirement> desiredValue, final BondFutureOptionSecurity security);

  protected ValueProperties getResultProperties(final String currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURRENCY, currency).get();
  }

  protected ValueProperties getResultProperties(final ValueRequirement desiredValue, final BondFutureOptionSecurity security) {
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String currency = security.getCurrency().getCode();
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURRENCY, currency).get();
  }

  protected FixedIncomeConverterDataProvider getDataConverter() {
    return _dataConverter;
  }

  protected BondFutureOptionTradeConverter getTradeConverter() {
    return _converter;
  }

  private ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.BOND_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

  private String getFutureOptionPrefix(final ComputationTarget target) {
    final ExternalIdBundle secId = target.getTrade().getSecurity().getExternalIdBundle();
    final String ticker = secId.getValue(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker != null) {
      final String prefix = ticker.substring(0, 2);
      return prefix;
    }
    throw new OpenGammaRuntimeException("Could not find option ticker");
  }

  private Surface<Double, Double, Double> getVolatilitySurface(final Surface<Double, Double, Double> surface, final double callPrice, final double putPrice, final double futureMarketPrice,
      final BondFutureOptionPremiumTransaction futureOption, final YieldCurveBundle data) {
    final BondFutureOptionPremiumSecurity underlyingOption = futureOption.getUnderlyingOption();
    final double futurePrice = BondFutureDiscountingMethod.getInstance().price(underlyingOption.getUnderlyingFuture(), data);
    final double strike = underlyingOption.getStrike();
    final double t = underlyingOption.getExpirationTime();
    double impliedVolatility;
    final boolean isCall = underlyingOption.isCall();
    try {
      if (isCall) {
        impliedVolatility = BlackFormulaRepository.impliedVolatility(callPrice, futurePrice, strike, t, true);
      } else {
        impliedVolatility = BlackFormulaRepository.impliedVolatility(putPrice, futurePrice, strike, t, false);
      }
    } catch (final IllegalArgumentException e) {
      if (isCall) {
        impliedVolatility = BlackFormulaRepository.impliedVolatility(putPrice, futurePrice, strike, t, false);
      } else {
        impliedVolatility = BlackFormulaRepository.impliedVolatility(callPrice, futurePrice, strike, t, true);
      }
    }
    if (!(surface instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Can only handle interpolated surfaces");
    }
    final InterpolatedDoublesSurface interpolatedSurface = (InterpolatedDoublesSurface) surface;
    final double[] x = interpolatedSurface.getXDataAsPrimitive();
    final double[] y = interpolatedSurface.getYDataAsPrimitive();
    final int n = x.length;
    final double[] z = new double[n];
    for (int i = 0; i < n; i++) {
      z[i] = impliedVolatility;
    }
    return InterpolatedDoublesSurface.from(x, y, z, interpolatedSurface.getInterpolator());
  }
}
