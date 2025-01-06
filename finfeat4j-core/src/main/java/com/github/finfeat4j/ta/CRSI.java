package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.ROC;

import java.math.BigDecimal;

/**
 * Connors RSI
 */
public class CRSI implements Indicator<BigDecimal, BigDecimal> {

    private final RSI rsi;
    private final RSI streak;
    private final Indicator<BigDecimal, BigDecimal> roc;
    private final Params params;
    private BigDecimal prevX;
    private int streakValue;

    public CRSI(int rsiPeriod, int streakPeriod, int rocPeriod) {
        this.rsi = new RSI(rsiPeriod);
        this.streak = new RSI(streakPeriod);
        this.roc = new ROC<BigDecimal>(rocPeriod).then(new RSI(rsiPeriod));
        this.params = new Params(rsiPeriod, streakPeriod, rocPeriod);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        if (this.prevX == null) {
            this.prevX = x;
        }
        var rsi = this.rsi.apply(x).doubleValue();
        var compareTo = x.compareTo(prevX);
        if (compareTo > 0) {
            streakValue = streakValue > 0 ? streakValue + 1 : 1;
        } else if (compareTo < 0) {
            streakValue = streakValue < 0 ? streakValue - 1 : -1;
        } else {
            streakValue = 0;
        }
        var streak = this.streak.apply(BigDecimal.valueOf(this.streakValue)).doubleValue();
        var rsiRoc = this.roc.apply(x).doubleValue();
        this.prevX = x;
        return round((rsi + streak + rsiRoc) / 3, 2);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
