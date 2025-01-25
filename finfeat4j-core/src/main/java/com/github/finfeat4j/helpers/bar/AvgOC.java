package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class AvgOC implements Indicator<Bar, BigDecimal> {
    private static final double factor = 0.5;

    @Override
    public BigDecimal apply(Bar bar) {
        double open = bar.open().doubleValue();
        double close = bar.close().doubleValue();
        return round((open + close) * factor, bar.close());
    }
}
