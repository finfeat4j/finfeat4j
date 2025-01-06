package com.github.finfeat4j.ta;


import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.helpers.MinMax;

import java.math.BigDecimal;

public class Fisher implements Indicator<BigDecimal, BigDecimal> {
    private final Buffer.DoubleBuffer buffer;
    private final MinMax<BigDecimal> minMax;
    private double coeff = 0.0;
    private double prevFisher = 0.0;
    private final Params params;

    public Fisher(int length) {
        this.buffer = new Buffer.DoubleBuffer(length);
        this.minMax = new MinMax<>(length);
        this.params = new Params(length);
    }

    private double round(double val) {
        return (val > 0.99) ? 0.999 : (val < -0.99) ? -0.999 : val;
    }

    @Override
    public BigDecimal apply(BigDecimal v) {
        this.buffer.addToEnd(v.doubleValue());

        if (!this.buffer.isFull()) {
            return BigDecimal.ZERO;
        }

        var minMax = this.minMax.apply(v);

        double high_ = minMax.max().doubleValue();
        double low_ = minMax.min().doubleValue();

        var thisPrice = this.buffer.tail();

        var value = 0.33 * 2 * ( (thisPrice - low_) / (high_ - low_) - 0.5) + 0.67 * this.coeff;

        if (Double.isNaN(value) || !Double.isFinite(value)) {
            value = 0;
        }
        this.coeff = this.round(value);
        this.prevFisher = 0.5 * Math.log((1 + this.coeff) / (1 - this.coeff)) + 0.5 * this.prevFisher;
        return round(prevFisher, 4);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
