package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.core.Bar;
import com.github.finfeat4j.core.Indicator;

public class HeikenAshi implements Indicator<Bar, Bar> {

    private final AvgPrice avgPrice = new AvgPrice();
    private double prevOpen = Double.NaN;
    private double prevClose;

    @Override
    public Bar apply(Bar bar) {
        var close = avgPrice.apply(bar);
        var o = bar.open();
        if (!Double.isNaN(this.prevOpen)) {
            o = round((this.prevOpen + this.prevClose) * 0.5, close.scale());
        }

        var h = bar.high().max(o).max(close);
        var l = bar.low().min(o).min(close);
        this.prevOpen = o.doubleValue();
        this.prevClose = close.doubleValue();
        return new Bar.BaseBar(bar.timestamp(), o, close, h, l, bar.vol(), bar.trades());
    }
}
