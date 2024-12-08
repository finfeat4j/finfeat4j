package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.core.Bar;
import com.github.finfeat4j.core.Indicator;
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
