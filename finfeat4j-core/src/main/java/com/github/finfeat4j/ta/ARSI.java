package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.helpers.ROC;

import java.math.BigDecimal;

/**
 *  Relative Strength Index (ARSI) indicator.
 */
public class ARSI implements Indicator<BigDecimal, BigDecimal> {
    private final Buffer.DoubleBuffer buffer;
    private double downSum = 0.0d;
    private double upSum = 0.0d;
    private final Params params;
    private final ROC<BigDecimal> roc;
    private int upCount;
    private BigDecimal arsi = BigDecimal.ZERO;

    public ARSI(int length) {
        this.buffer = new Buffer.DoubleBuffer(length);
        this.params = new Params(length);
        this.roc = new ROC<>(1);
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    @Override
    public BigDecimal apply(BigDecimal v) {
        if (this.buffer.isFull()) {
            var value = this.buffer.head();
            if (value >= 0) {
                this.upCount--;
            }
        }
        double roc = this.roc.apply(v).doubleValue();
        if (roc >= 0) {
            this.upCount++;
        }
        this.buffer.addToEnd(roc);
        int downCount = this.buffer.size() - upCount;

        double upAlpha = upCount > 0 ? (1 / (double) upCount) : 0;
        double downAlpha = downCount > 0 ? (1 / (double) downCount) : 0;

        this.upSum = upAlpha * (roc >= 0 ? roc : 0) + ((1 - upAlpha) * this.upSum);
        this.downSum = downAlpha * (roc >= 0 ? 0 : Math.abs(roc)) + ((1 - downAlpha) * this.downSum);

        double arsi = 100 * (this.upSum / (this.upSum + this.downSum));
        if (!Double.isNaN(arsi) && Double.isFinite(arsi)) {
            this.arsi = round(arsi, 2);
        }
        return this.arsi;
    }
}