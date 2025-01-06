package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer.ObjBuffer;
import com.github.finfeat4j.util.SortedVector;
import java.math.BigDecimal;

/**
 * Simple Moving Median
 */
public class SMM implements Indicator<BigDecimal, BigDecimal> {

    private final SortedVector vector;
    private final ObjBuffer<BigDecimal> buffer;

    private final Params params;

    public SMM(int size) {
        this.vector = new SortedVector(size);
        this.buffer = new ObjBuffer<>(size);
        this.params = new Params(size);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        var v = x.doubleValue();
        if (this.buffer.isFull()) {
            vector.remove(this.buffer.get(0).doubleValue());
            vector.insertIntoOpenSlot(v);
        } else {
            vector.append(v);
        }
        this.buffer.addToEnd(x);
        return this.buffer.isFull() ? round(vector.getMedian(), x) : x;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
