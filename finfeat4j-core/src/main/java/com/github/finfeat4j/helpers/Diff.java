package com.github.finfeat4j.helpers;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class Diff<T, R extends Number> implements Indicator<T, BigDecimal> {

        private final Indicator<T, R> a;
        private final Indicator<T, R> b;

        public Diff(Indicator<T, R> a, Indicator<T, R> b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public BigDecimal apply(T value) {
            var a = this.a.apply(value);
            var b = this.b.apply(value);
            return BigDecimal.valueOf(a.doubleValue() - b.doubleValue());
        }
}
