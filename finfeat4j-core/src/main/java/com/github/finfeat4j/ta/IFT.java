package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.WMA;

import java.math.BigDecimal;

/**
 * Inverse Fisher Transform (IFT) indicator.
 */
public abstract class IFT<T> implements Indicator<T, BigDecimal> {
    private final Indicator<T, BigDecimal> indicator;

    private final Indicator<BigDecimal, BigDecimal> smooth;

    private final double fraction;

    public IFT(Indicator<T, BigDecimal> indicator, Indicator<BigDecimal, BigDecimal> ma, double fraction) {
        this.indicator = indicator;
        this.fraction = fraction;
        this.smooth = ma;
    }

    public IFT(Indicator<T, BigDecimal> indicator, int smoothLength, double fraction) {
        this(indicator, new WMA(smoothLength), fraction);
    }

    @Override
    public BigDecimal apply(T v) {
        double rsiVal = this.indicator.apply(v).doubleValue();
        double rsiCalc = 0.1 * (rsiVal - this.fraction);
        double wmaVal = this.smooth.apply(BigDecimal.valueOf(rsiCalc)).doubleValue();
        double calc = (Math.exp( 2 * wmaVal) - 1) / (Math.exp(2 * wmaVal) + 1);
        if (Double.isNaN(calc)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(calc);
    }
}