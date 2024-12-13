package com.github.finfeat4j.util;

import com.github.finfeat4j.core.Indicator;

/**
 * ArrayProducer is a class that produces an array of values from an indicator.
 * Any indicator that produces an array of values should be wrapped with this class.
 * @param <T>
 * @param <R>
 */
public class ArrayProducerImpl<T, R> implements ArrayProducer<T, R> {

    private final Indicator<T, R> indicator;
    private final int size;

    public ArrayProducerImpl(Indicator<T, R> indicator, int size) {
        this.indicator = indicator;
        this.size = size;
    }

    @Override
    public R apply(T t) {
        return this.indicator.apply(t);
    }

    public int size() {
        return this.size;
    }

    @Override
    public String getName(Object... attrs) {
        return indicator.getName(attrs);
    }
}
