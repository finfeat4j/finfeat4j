package com.github.finfeat4j.others;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class NormFilter implements Indicator<BigDecimal, BigDecimal> {
    private double min;
    private double max;

    public NormFilter(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public NormFilter() {
        this(Double.MAX_VALUE, Double.MIN_VALUE);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        var xVal = x.doubleValue();
        if (xVal > max) {
            this.max = xVal;
        }
        if (xVal < min) {
            this.min = xVal;
        }
        if (max - min == 0) {
            return BigDecimal.ZERO;
        }
        var normValue = (xVal - min) / (max - min);
        if (Double.isNaN(normValue) || !Double.isFinite(normValue)) {
            normValue = 0;
        }
        return BigDecimal.valueOf(normValue);
    }
}
