package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.MMA;

import java.math.BigDecimal;

/**
 * Relative Strength Index (RSI) indicator.
 */
public class RSI implements Indicator<BigDecimal, BigDecimal> {

    private final MMA avgLoss;
    private final MMA avgGain;
    private final Params params;
    private BigDecimal prevValue;

    public RSI(int period) {
        this.avgLoss = new MMA(period);
        this.avgGain = new MMA(period);
        this.params = new Params(period);
    }

    @Override
    public BigDecimal apply(BigDecimal v) {
        double change = (prevValue != null) ? v.subtract(prevValue).doubleValue() : 0;
        prevValue = v;

        double avgGain = this.avgGain.apply(change > 0 ? BigDecimal.valueOf(change) : BigDecimal.ZERO).doubleValue();
        double avgLoss = this.avgLoss.apply(change <= 0 ? BigDecimal.valueOf(-change) : BigDecimal.ZERO).doubleValue();

        double rsi = (avgLoss == 0) ? (avgGain == 0 ? 0 : 100) : 100 - (100 / (1 + (avgGain / avgLoss)));
        return round(rsi, 2);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
