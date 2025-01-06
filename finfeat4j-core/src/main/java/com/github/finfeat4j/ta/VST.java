package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.stats.Stats;

import java.math.BigDecimal;

public class VST implements Indicator<BigDecimal, BigDecimal> {
    private final Stats stats;

    public VST(int length) {
        this.stats = new Stats(length);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        var stat = this.stats.apply(x);
        var stdDev = stat.stdDev();
        var r = x.doubleValue() / stdDev;
        return Double.isNaN(r) || Double.isInfinite(r) ? BigDecimal.ZERO : BigDecimal.valueOf(r);
    }

    @Override
    public Params getParams() {
        return this.stats.getParams();
    }
}
