package com.github.finfeat4j.helpers;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;

import java.math.BigDecimal;

public class Sum implements Indicator<Number, BigDecimal> {

    private final Buffer.DoubleBuffer buffer;
    private final Params params;
    private double sum;

    public Sum(int length) {
        this.buffer = new Buffer.DoubleBuffer(length);
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(Number number) {
        if (this.buffer.isFull()) {
            this.sum -= this.buffer.get(0);
        }
        var val = number.doubleValue();
        this.buffer.addToEnd(val);
        this.sum += val;
        return BigDecimal.valueOf(this.sum);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
