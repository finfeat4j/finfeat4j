package com.github.finfeat4j.helpers;

import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.core.Indicator;

import java.math.BigDecimal;

public class ROC<T extends Number> implements Indicator<T, BigDecimal> {

        private final Buffer.ObjBuffer<T> buffer;
        private final Params params;

        public ROC(int length) {
            this.buffer = new Buffer.ObjBuffer<>(length + 1);
            this.params = new Params(length);
        }

        @Override
        public BigDecimal apply(T value) {
            buffer.addToEnd(value);
            var a = buffer.head().doubleValue();
            var b = buffer.tail().doubleValue();
            if (b != 0) {
                return BigDecimal.valueOf(((a - b) / b) * 100.0d);
            }
            return BigDecimal.ZERO;
        }

    @Override
    public Params getParams() {
        return this.params;
    }
}
