package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.core.Bar;
import com.github.finfeat4j.core.Indicator;
import java.math.BigDecimal;

public class TypicalPrice implements Indicator<Bar, BigDecimal> {
    private static final double factor = 1 / (double) 3;

    @Override
    public BigDecimal apply(Bar bar) {
        double close = bar.close().doubleValue();
        double high = bar.high().doubleValue();
        double low = bar.low().doubleValue();
        return round((close + high + low) * factor, bar.close());
    }
}
