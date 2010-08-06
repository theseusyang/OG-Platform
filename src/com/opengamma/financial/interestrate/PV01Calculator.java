/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.util.tuple.Pair;

/**
 * Returns the change in present value of an instrument due to a parallel move of the yield curve, scaled so that the move is 1bp.
 *  
 */
public class PV01Calculator {

  private final PresentValueSensitivityCalculator _pvsc = PresentValueSensitivityCalculator.getInstance();

  /**
   * Calculates the change in present value of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird instrument 
   * @param curves bundle of relevant yield curves 
   * @return a Map between curve name and PV01 for that curve 
   */
  public Map<String, Double> getValue(InterestRateDerivative ird, YieldCurveBundle curves) {

    Map<String, List<Pair<Double, Double>>> sense = _pvsc.getValue(ird, curves);
    Map<String, Double> res = new HashMap<String, Double>();
    Iterator<Entry<String, List<Pair<Double, Double>>>> iterator = sense.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, List<Pair<Double, Double>>> entry = iterator.next();
      String name = entry.getKey();
      double pv01 = sumListPair(entry.getValue()) / 10000.;
      res.put(name, pv01);
    }

    return res;

  }

  private double sumListPair(List<Pair<Double, Double>> list) {
    double sum = 0.0;
    for (Pair<Double, Double> pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }

}
