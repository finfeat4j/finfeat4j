package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

public class Trades implements Indicator<Bar, Long> {

    @Override
    public Long apply(Bar bar) {
        return bar.trades();
    }
}
