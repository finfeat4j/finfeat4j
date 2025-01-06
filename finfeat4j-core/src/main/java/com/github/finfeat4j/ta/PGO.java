package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.MMA;
import com.github.finfeat4j.ta.ma.SMA;

import java.math.BigDecimal;

/**
 * Pretty Good Oscillator
 */
public class PGO implements Indicator<Bar, BigDecimal> {
    private final Indicator<Bar, BigDecimal> atr;
    private final Indicator<Bar, BigDecimal> target;
    private final Indicator<BigDecimal, BigDecimal> targetSma;
    private BigDecimal lastValue = BigDecimal.ZERO;
    private final Params params;

    public PGO(int length, Indicator<Bar, BigDecimal> target, Indicator<BigDecimal, BigDecimal> targetSma) {
        this.atr = new TR().then(new MMA(length));
        this.target = target;
        this.targetSma = targetSma;
        this.params = new Params(length);
    }

    public PGO(int length, Indicator<Bar, BigDecimal> target) {
        this(length, target, new SMA(length));
    }

    @Override
    public BigDecimal apply(Bar bar) {
        var target = this.target.apply(bar);
        var atr = this.atr.apply(bar).doubleValue();
        var targetSma = this.targetSma.apply(target).doubleValue();
        var pgo = (target.doubleValue() - targetSma) / atr;
        if (!Double.isNaN(pgo) && Double.isFinite(pgo)) {
            this.lastValue = BigDecimal.valueOf(pgo);
        }
        return this.lastValue;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
