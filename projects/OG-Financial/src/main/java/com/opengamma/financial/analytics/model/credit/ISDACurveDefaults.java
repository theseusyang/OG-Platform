/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDACurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ISDACurveDefaults.class);
  private final PriorityClass _priority;
  private final Map<String, String> _offsetsForCurrency;

  public ISDACurveDefaults(final String priority, final String... currencyAndOffsets) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(currencyAndOffsets, "currency and offsets");
    ArgumentChecker.isTrue(currencyAndOffsets.length % 2 == 0, "must have one offset per currency");
    _priority = PriorityClass.valueOf(priority);
    _offsetsForCurrency = new LinkedHashMap<String, String>();
    for (int i = 0; i < currencyAndOffsets.length; i += 2) {
      _offsetsForCurrency.put(currencyAndOffsets[i], currencyAndOffsets[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uid = target.getUniqueId();
    if (!Currency.OBJECT_SCHEME.equals(uid.getScheme())) {
      return false;
    }
    return _offsetsForCurrency.containsKey(uid.getValue());
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, ISDAFunctionConstants.ISDA_CURVE_OFFSET);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = target.getUniqueId().getValue();
    final String offset = _offsetsForCurrency.get(currency);
    if (offset != null) {
      return Collections.singleton(offset);
    }
    s_logger.error("Could not get number of offset days for {}; should never happen", currency);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }
}
