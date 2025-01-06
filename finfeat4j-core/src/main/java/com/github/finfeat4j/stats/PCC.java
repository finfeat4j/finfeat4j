package com.github.finfeat4j.stats;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;

import java.math.BigDecimal;

/**
 * Pearson Correlation Coefficient (PCC) indicator.
 */
public class PCC implements Indicator<BigDecimal, BigDecimal> {
    private final Buffer.DoubleBuffer src;
    private double sumX, sumY, sumXY, sumX2, sumY2;
    private long globalIndex;
    private final Params params;

    public PCC(int length) {
        this.src = new Buffer.DoubleBuffer(length);
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

        src.addToEnd(newValue);

        var currentX = globalIndex;

        sumX += currentX;
        sumY += newValue;
        sumXY += currentX * newValue;
        sumX2 += currentX * currentX;
        sumY2 += newValue * newValue;

        if (src.isFull()) {
            int len = src.size();
            var coeff1 = len*sumX2-sumX*sumX;
            var coeff2 = len*sumY2-sumY*sumY;
            if (coeff1 > 0 && coeff2 > 0) {
                var correlation = (len * sumXY - sumX * sumY) / Math.sqrt(coeff1*coeff2);
                result = BigDecimal.valueOf(correlation);
            }
        }
        return result;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
