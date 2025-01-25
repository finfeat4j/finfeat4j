package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class MedianPrice implements Indicator<Bar, BigDecimal> {
    private static final double factor = 0.5;

    @Override
    public BigDecimal apply(Bar bar) {
        double high = bar.high().doubleValue();
        double low = bar.low().doubleValue();
        return round((high + low) * factor, bar.close());
    }
}
