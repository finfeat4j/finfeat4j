package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.bar.Close;
import com.github.finfeat4j.helpers.bar.High;
import com.github.finfeat4j.helpers.bar.Low;
import com.github.finfeat4j.helpers.bar.Open;
import java.math.BigDecimal;

/**
 * A New Adaptive Moving Average (Vama) Technical Indicator For Financial Data Smoothing
 * Pierrefeu, Alex (2019): A New Adaptive Moving Average (Vama) Technical Indicator
 * For Financial Data Smoothing.
 * https://mpra.ub.uni-muenchen.de/94323/
 */
public class VAMA implements Indicator<Bar, BigDecimal> {

    private final Params params;

    private final Indicator<Bar, BigDecimal> open;
    private final Indicator<Bar, BigDecimal> close;
    private final Indicator<Bar, BigDecimal> high;
    private final Indicator<Bar, BigDecimal> low;

    private double lastValue = Double.NaN;

    public VAMA(int period) {
        this.open = new Open().then(new SMA(period));
        this.close = new Close().then(new SMA(period));
        this.high = new High().then(new SMA(period));
        this.low = new Low().then(new SMA(period));
        this.params = new Params(period);
    }

    @Override
    public BigDecimal apply(Bar bar) {
        var c = getValue(this.close, bar);
        var o = getValue(this.open, bar);
        var h = getValue(this.high, bar);
        var l = getValue(this.low, bar);
        var b = h - l;
        var lv = 1.0d;
        if (b != 0) {
            lv = Math.abs(c - o) / b;
        }
        this.lastValue = !Double.isNaN(this.lastValue) ? this.lastValue : c;
        this.lastValue = (lv * c) + ((1 - lv) * this.lastValue);
        return round(this.lastValue, bar.close());
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    private double getValue(Indicator<Bar, BigDecimal> indicator, Bar bar) {
        return indicator.apply(bar).doubleValue();
    }
}
