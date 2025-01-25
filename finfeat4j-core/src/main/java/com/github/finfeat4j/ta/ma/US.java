package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;

import java.math.BigDecimal;

/**
 * Ultimate Smoother (US) indicator.
 */
public class US implements Indicator<BigDecimal, BigDecimal> {
    private final double c2;
    private final double c3;
    private final double c4;
    private final double c5;
    private final double c6;
    private final Buffer.DoubleBuffer src, us;

    private final Params params;

    public US(int length) {
        double f = (1.414 * Math.PI) / length;
        double a1 = Math.exp(-f);
        this.c2 = 2 * a1 * Math.cos(f);
        this.c3 = -a1 * a1;
        double c1 = (1 + c2 - c3) / 4;
        this.c4 = 1 - c1;
        this.c5 = 2 * c1 - c2;
        this.c6 = c1 + c3;
        this.src = new Buffer.DoubleBuffer(2);
        this.us = new Buffer.DoubleBuffer(2);
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(BigDecimal value) {
        double val = value.doubleValue();
        if (!src.isFull()) {
            us.addToEnd(val);
            src.addToEnd(val);
            return value;
        } else {
            double newVal = (c4 * val) + (c5 * src.getR(0)) - (c6 * src.getR(1)) + (c2 * us.getR(0)) + (c3 * us.getR(1));
            us.addToEnd(newVal);
            src.addToEnd(val);
            return round(newVal, value);
        }
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
