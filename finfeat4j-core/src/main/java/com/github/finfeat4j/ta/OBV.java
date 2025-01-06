package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

/**
 * On Balance Volume (OBV) indicator.
 */
public class OBV implements Indicator<Bar, BigDecimal> {
    private BigDecimal obv = BigDecimal.ZERO;
    private BigDecimal prevClose;

    @Override
    public BigDecimal apply(Bar bar) {
        var close = bar.close().doubleValue();
        var vol = bar.vol();
        if (prevClose == null) {
            prevClose = BigDecimal.valueOf(close);
            return this.obv;
        }
        if (close > prevClose.doubleValue()) {
            this.obv = this.obv.add(vol);
        } else {
            this.obv = this.obv.subtract(vol);
        }
        return this.obv;
    }
}
