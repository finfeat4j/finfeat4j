package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.stats.Stats;

import java.math.BigDecimal;

public class VSCT implements Indicator<BigDecimal, BigDecimal> {
    private final Stats stats;

    public VSCT(int period) {
        this.stats = new Stats(period);
    }

    @Override
    public BigDecimal apply(BigDecimal x) {
        var stat = this.stats.apply(x);
        var mean = stat.mean();
        var std = stat.stdDev();
        var r = (x.doubleValue() - mean) / std;
        return Double.isNaN(r) || Double.isInfinite(r) ? BigDecimal.ZERO : BigDecimal.valueOf(r);
    }

    @Override
    public Params getParams() {
        return this.stats.getParams();
    }
}
