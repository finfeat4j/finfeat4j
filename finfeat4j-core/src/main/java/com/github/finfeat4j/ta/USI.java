package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.SMA;
import com.github.finfeat4j.ta.ma.US;

import java.math.BigDecimal;

/**
 * Ultimate Strength Index (USI) indicator.
 */
public class USI implements Indicator<BigDecimal, BigDecimal> {
    private final Params params;
    private final Indicator<BigDecimal, BigDecimal> usuUp;
    private final Indicator<BigDecimal, BigDecimal> usuDown;
    private BigDecimal prevX;

    public USI(int length, int smaLen) {
        this.params = new Params(length, smaLen);
        this.usuUp = new SMA(smaLen).then(new US(length));
        this.usuDown = new SMA(smaLen).then(new US(length));
    }
    @Override
    public BigDecimal apply(BigDecimal v) {
        double SU = (prevX != null) ? v.subtract(prevX).doubleValue() : 0;
        double SD = (prevX != null) ? prevX.subtract(v).doubleValue() : 0;
        prevX = v;
        double avgGain = this.usuUp.apply(BigDecimal.valueOf(Math.max(0, SU))).doubleValue();
        double avgLoss = this.usuDown.apply(BigDecimal.valueOf(Math.max(0, SD))).doubleValue();
        var usi = (avgGain - avgLoss) / (avgGain + avgLoss);
        if (!Double.isNaN(usi) && Double.isFinite(usi)) {
            return BigDecimal.valueOf(usi);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
