package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.MinMax;

import java.math.BigDecimal;

/**
 * Williams %R (WLR) indicator.
 */
public class WLR implements Indicator<Bar, BigDecimal> {
    private final MinMax<BigDecimal> minMaxHigh;
    private final MinMax<BigDecimal> minMaxLow;

    private final Params params;

    public WLR(int period) {
        this.minMaxHigh = new MinMax<>(period);
        this.minMaxLow = new MinMax<>(period);
        this.params = new Params(period);
    }

    @Override
    public BigDecimal apply(Bar bar) {
        var minMaxHigh = this.minMaxHigh.apply(bar.high()).max().doubleValue();
        var minMaxLow = this.minMaxLow.apply(bar.low()).min().doubleValue();
        var diff = minMaxHigh - minMaxLow;
        var value = diff != 0 ? ((minMaxHigh - bar.close().doubleValue()) / diff) * -100 : -100;
        return BigDecimal.valueOf(value);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
