package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;

import java.math.BigDecimal;

/**
 * Noise elimination technology
 */
public class NET implements Indicator<BigDecimal, BigDecimal> {

    private final Buffer.DoubleBuffer buffer;
    private final Params params;

    public NET(int length) {
        this.params = new Params(length);
        this.buffer = new Buffer.DoubleBuffer(length);
    }

    @Override
    public BigDecimal apply(BigDecimal value) {
        buffer.addToEnd(value.doubleValue());
        double num = 0;
        for (int count = 1; count < buffer.size(); count++) {
            for (int k = 0; k < count; k++) {
                num -= Math.signum(buffer.getR(count) - buffer.getR(k));
            }
        }
        double denom = 0.5d * buffer.size() * (buffer.size() - 1);
        return denom != 0 ? BigDecimal.valueOf(num / denom) : BigDecimal.ZERO;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}