package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;

import java.math.BigDecimal;

/**
 * Trend Strength Index
 */
public class TSI implements Indicator<BigDecimal, BigDecimal> {
    private final Buffer.DoubleBuffer src;
    private double sumX, sumY, sumXY, sumX2, sumY2;
    private long globalIndex;
    private final Params params;

    public TSI(int length) {
        this.src = new Buffer.DoubleBuffer(length);
        this.sumX = 0;
        this.sumY = 0;
        this.sumXY = 0;
        this.sumX2 = 0;
        this.sumY2 = 0;
        this.globalIndex = 0;
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(BigDecimal y) {
        var result = BigDecimal.ZERO;
        var newValue = y.doubleValue();

        globalIndex++; // Increment the global index for each new value

        if (src.isFull()) {
            // Remove the oldest value's contribution
            var oldestValue = src.head();
            var oldestIndex = globalIndex - src.size(); // Oldest index based on global counter

            sumX -= oldestIndex;
            sumY -= oldestValue;
            sumXY -= oldestIndex * oldestValue;
            sumX2 -= oldestIndex * oldestIndex;
            sumY2 -= oldestValue * oldestValue;
        }

        // Add the new value's contribution
        src.addToEnd(newValue);

        sumX += globalIndex;
        sumY += newValue;
        sumXY += globalIndex * newValue;
        sumX2 += globalIndex * globalIndex;
        sumY2 += newValue * newValue;

        // Calculate TSI if buffer is full
        if (src.isFull()) {
            int len = src.size();
            double numerator = (len * sumXY) - (sumX * sumY);
            double denominator = Math.sqrt(
                    (len * sumX2 - sumX * sumX) *
                            (len * sumY2 - sumY * sumY)
            );

            if (denominator != 0) {
                result = round((numerator / denominator) * -1, 4);
            }
        }

        return result;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}