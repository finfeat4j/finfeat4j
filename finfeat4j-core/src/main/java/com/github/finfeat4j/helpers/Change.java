package com.github.finfeat4j.helpers;

import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class Change implements Indicator<BigDecimal, BigDecimal> {

    private final Buffer.DoubleBuffer buffer;
    private final Params params;

    public Change(int period) {
        this.buffer = new Buffer.DoubleBuffer(period + 1);
        this.params = new Params(period);
    }

    @Override
    public BigDecimal apply(BigDecimal value) {
        buffer.addToEnd(value.doubleValue());
        if (buffer.isFull()) {
            return BigDecimal.valueOf(buffer.tail() - buffer.head());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
