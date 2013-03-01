/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed compounded coupon. 
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+\delta_i r) \right)-1
 * \end{equation*}
 * $$
 * where the $\delta_i$ are the accrual factors of the sub periods and the $r$ the fixed rate.
 * dates used to compute the coupon accrual factors.
 */
public class CouponFixedCompounding extends Coupon {

  /**
   * The fixed rate.
   * All the coupon sub-periods use the same fixed rate.
   */
  private final double _rate;

  /**
   * The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   */
  private final double[] _paymentAccrualFactors;

  /**
   * The notional accrued is the amount equal to
  * $$
  * \begin{equation*}
  * \notional \left(\prod_{i=1}^n (1+\delta_i r) \right)
  * \end{equation*}
  * $$
   */
  private final double _notionalAccrued;

  /**
   * Constructor.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional The coupon notional.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   * @param rate the fixed rate. 
   */
  public CouponFixedCompounding(Currency currency, double paymentTime, double paymentYearFraction, double notional,
      double[] paymentAccrualFactors, double rate) {
    super(currency, paymentTime, "", paymentYearFraction, notional);
    _paymentAccrualFactors = paymentAccrualFactors;
    _rate = rate;
    final int nbSubPeriod = paymentAccrualFactors.length;
    double notionalAccrued = notional;
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {

      notionalAccrued *= 1 + paymentAccrualFactors[loopsub] * rate;
    }
    _notionalAccrued = notionalAccrued;
  }

  public double getRate() {
    return _rate;
  }

  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  public double getNotionalAccrued() {
    return _notionalAccrued;
  }

  @Override
  public Coupon withNotional(double notional) {
    return new CouponFixedCompounding(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, _paymentAccrualFactors, _rate);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedCompounding(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedCompounding(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponFixedCompounding other = (CouponFixedCompounding) obj;
    if (!Arrays.equals(_paymentAccrualFactors, other._paymentAccrualFactors)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
