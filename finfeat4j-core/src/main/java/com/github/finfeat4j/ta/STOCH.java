package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.MinMax;

import java.math.BigDecimal;

/**
 * Stochastic Oscillator (STOCH) indicator.
 */
public class STOCH implements Indicator<BigDecimal, BigDecimal> {
    private final MinMax<BigDecimal> minMax;
    private final Params params;

    public STOCH(int length) {
        this.minMax = new MinMax<>(length);
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(BigDecimal value) {
        var minMax = this.minMax.apply(value);
        var min = minMax.min().doubleValue();
        var max = minMax.max().doubleValue();
        var newValue = value.doubleValue() - min / max - min;
        if (!Double.isNaN(newValue) && Double.isFinite(newValue)) {
            return round(newValue, 4);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
