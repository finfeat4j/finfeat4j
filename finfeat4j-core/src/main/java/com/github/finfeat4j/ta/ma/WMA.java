package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer.DoubleBuffer;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * Weighted Moving Average (WMA).
 */
public class WMA implements Indicator<BigDecimal, BigDecimal> {

    private final double coeff;
    private final DoubleBuffer buffer;
    private final Params params;

    public WMA(int length) {
        this.coeff = 1.0 / ((double) (length * (length + 1) / 2));
        this.buffer = new DoubleBuffer(length);
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        this.buffer.addToEnd(x.doubleValue());
        if (!this.buffer.isFull()) {
            return x;
        }
        // slow because of this loop
        var wma = IntStream.range(0, this.buffer.size())
            .mapToDouble(i -> (this.buffer.get(i) * (i + 1)) * this.coeff)
            .sum();
        return round(wma, x);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
