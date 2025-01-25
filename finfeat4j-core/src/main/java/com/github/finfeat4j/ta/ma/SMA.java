package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer.DoubleBuffer;

import java.math.BigDecimal;

public class SMA implements Indicator<BigDecimal, BigDecimal> {

    private final Params params;

    private final DoubleBuffer buffer;
    private double sum = 0;

    public SMA(int length) {
        this.params = new Params(length);
        this.buffer = new DoubleBuffer(length);
    }

    @Override
    public BigDecimal apply(BigDecimal v) {
        if (this.buffer.isFull()) {
            this.sum -= this.buffer.head();
        }
        this.buffer.addToEnd(v.doubleValue());
        this.sum += this.buffer.tail();
        return round(this.sum / buffer.size(), v);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
