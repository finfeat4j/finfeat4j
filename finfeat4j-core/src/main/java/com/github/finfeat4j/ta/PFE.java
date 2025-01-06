package com.github.finfeat4j.ta;

import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.EMA;

import java.math.BigDecimal;

/**
 * Polarized Fractal Efficiency (PFE) indicator.
 */
public class PFE implements Indicator<BigDecimal, BigDecimal> {

    private final Buffer.DoubleBuffer buffer;
    private final Indicator<BigDecimal, BigDecimal> ma;
    private final Params params;

    public PFE(int length, Indicator<BigDecimal, BigDecimal> ma, Params params) {
        this.buffer = new Buffer.DoubleBuffer(length);
        this.ma = ma;
        this.params = params;
    }

    public PFE(int length, int emaLength) {
        this(length, new EMA(emaLength), new Params(length, emaLength));
    }

    @Override
    public BigDecimal apply(BigDecimal val) {
        double value = val.doubleValue();
        this.buffer.addToEnd(value);

        if (this.buffer.isFull()) {
            double s = 0.0d;
            int wl = this.buffer.size() - 1;
            for (int i = 0; i < wl; i++) {
                double v0 = this.buffer.get(wl - i);
                double v1 = this.buffer.get(wl - i - 1);
                s += Math.sqrt(Math.pow(v0 - v1, 2) + 1);
            }

            double deltaPrice = value - this.buffer.head();
            double p = Math.sqrt(Math.pow(deltaPrice, 2) + Math.pow(this.buffer.size(), 2)) / s;

            p *= Math.signum(deltaPrice);

            if (Double.isFinite(p) && !Double.isNaN(p)) {
                return this.ma.apply(BigDecimal.valueOf(p));
            }
        }

        return BigDecimal.ZERO; // Not enough data yet
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}