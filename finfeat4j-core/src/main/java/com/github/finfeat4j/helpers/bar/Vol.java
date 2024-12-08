package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.core.Bar;
import com.github.finfeat4j.core.Indicator;
import java.math.BigDecimal;

public class Vol implements Indicator<Bar, BigDecimal> {

    @Override
    public BigDecimal apply(Bar bar) {
        return bar.vol();
    }
}
