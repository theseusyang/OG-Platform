/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.CDSApproxISDAMethod;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.ISDACDSSecurityConverter;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Price CDS contracts according to the ISDA model using a hazard rate term structure
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see CDSApproxISDAMethod
 */
public class ISDAApproxCDSPriceHazardCurveFunction extends ISDAApproxCDSPriceFunction {

  private static final CDSApproxISDAMethod ISDA_APPROX_METHOD = new CDSApproxISDAMethod();
  
  @Override
  protected String getHazardRateStructre() {
    return ISDAFunctionConstants.ISDA_HAZARD_RATE_TERM;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {

      final CDSSecurity cds = (CDSSecurity) target.getSecurity();

      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      
      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(),
        ValueProperties
          .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get()));
      
      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(),
        ValueProperties
          .with(ValuePropertyNames.CURVE, "HAZARD_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
          .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get()));

      return requirements;
    }
    return null;
  }

  @Override
  public DoublesPair executeImpl(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    
    // Set up converter (could this be compiled?)
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ISDACDSSecurityConverter converter = new ISDACDSSecurityConverter(holidaySource);
    
    // Time point to price for
    // TODO: Where to get step-in and settlement
    final ZonedDateTime pricingDate = executionContext.getValuationClock().zonedDateTime();
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = pricingDate.plusDays(3);
    
    // Security being priced
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();

    // Discount curve
    final ISDACurve discountCurve = (ISDACurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get()));
    
    // Hazard rate curve
    final ISDACurve hazardRateCurve = (ISDACurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties
        .with(ValuePropertyNames.CURVE, "HAZARD_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
        .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get()));
    
    // Convert security in to format suitable for pricing
    final ISDACDSDefinition cdsDefinition = (ISDACDSDefinition) cds.accept(converter);
    final ISDACDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, stepinDate, settlementDate, discountCurve.getName(), hazardRateCurve.getName());
    
    // Go price!
    final double dirtyPrice = ISDA_APPROX_METHOD.calculateUpfrontCharge(cdsDerivative, discountCurve, hazardRateCurve, false);
    final double cleanPrice = dirtyPrice - cdsDerivative.getAccruedInterest();
    
    return DoublesPair.of(cleanPrice, dirtyPrice);
  }



}
