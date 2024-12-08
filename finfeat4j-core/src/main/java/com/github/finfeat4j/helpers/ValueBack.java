package com.github.finfeat4j.helpers;

import com.github.finfeat4j.core.Buffer.ObjBuffer;
import com.github.finfeat4j.core.Indicator;

/**
 * ValueBack indicator returns the previous value of the input.
 * @param <T> the type of the input and output
 */
public class ValueBack<T> implements Indicator<T, T> {

    private final ObjBuffer<T> buffer;
    private final Params params;

    public ValueBack(int length) {
        this.buffer = new ObjBuffer<>(length + 1);
        this.params = new Params(length);
    }

    @Override
    public T apply(T t) {
        this.buffer.addToEnd(t);
        return this.buffer.head();
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
