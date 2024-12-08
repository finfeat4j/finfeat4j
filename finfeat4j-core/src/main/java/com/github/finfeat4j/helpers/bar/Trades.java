package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.core.Bar;
import com.github.finfeat4j.core.Indicator;

public class Trades implements Indicator<Bar, Long> {

    @Override
    public Long apply(Bar bar) {
        return bar.trades();
    }
}
