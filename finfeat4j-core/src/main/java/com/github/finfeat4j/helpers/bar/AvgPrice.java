package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class AvgPrice implements Indicator<Bar, BigDecimal> {
    private static final double factor = 0.25;

    @Override
    public BigDecimal apply(Bar bar) {
        double close = bar.close().doubleValue();
        double low = bar.low().doubleValue();
        double high = bar.high().doubleValue();
        double open = bar.open().doubleValue();
        return round((close + open + high + low) * factor, bar.close());
    }
}
