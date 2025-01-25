package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

/**
 * Linear Decay (DEC) indicator.
 */
public class DEC implements Indicator<BigDecimal, BigDecimal> {

    private final Params params;
    private double coeff;
    private double prevDecay = Double.NaN;

    public DEC(int length) {
        this.params = new Params(length);
        this.coeff = 1.0d / length;
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        if (Double.isNaN(prevDecay)) {
            prevDecay = x.doubleValue();
            return x;
        }
        var decay = Math.max(0, Math.max(x.doubleValue(), prevDecay - coeff));
        prevDecay = decay;
        return BigDecimal.valueOf(decay);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
