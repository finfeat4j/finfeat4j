package com.github.finfeat4j.helpers.bar;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import java.math.BigDecimal;

public class High implements Indicator<Bar, BigDecimal> {

    @Override
    public BigDecimal apply(Bar bar) {
        return bar.high();
    }
}
