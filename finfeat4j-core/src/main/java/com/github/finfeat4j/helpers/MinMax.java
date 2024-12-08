package com.github.finfeat4j.helpers;

import com.github.finfeat4j.core.Buffer.ObjBuffer;
import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.helpers.MinMax.Result;
import com.github.finfeat4j.util.SortedVector;
import java.math.BigDecimal;

/**
 * Sliding window minimum and maximum indicator.
 * @param <T> any comparable type
 */
public class MinMax<T extends Number> implements Indicator<T, Result<BigDecimal>> {

    private final SortedVector vector;
    private final ObjBuffer<T> buffer;
    private final Params params;
    private double tempMin = Double.POSITIVE_INFINITY;
    private double tempMax = Double.NEGATIVE_INFINITY;

    public MinMax(int length) {
        this.vector = new SortedVector(length);
        this.buffer = new ObjBuffer<>(length);
        this.params = new Params(length);
    }

    @Override
    public Result<BigDecimal> apply(T value) {
        var min = value.doubleValue();
        var max = value.doubleValue();
        if (buffer.isFull()) {
            var removedValue = buffer.head();
            vector.remove(removedValue.doubleValue());
            vector.insertIntoOpenSlot(value.doubleValue());
            min = vector.getMin();
            max = vector.getMax();
        } else {
            vector.append(value.doubleValue());
            min = tempMin = Math.min(tempMin, min);
            max = tempMax = Math.max(tempMax, max);
        }
        buffer.addToEnd(value);
        return new Result<>(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    public record Result<T extends Number>(T min, T max) {

    }
}