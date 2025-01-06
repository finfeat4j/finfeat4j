package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

/**
 * True Range (TR) indicator.
 */
public class TR implements Indicator<Bar, BigDecimal> {

    private BigDecimal prevClose;

    @Override
    public BigDecimal apply(Bar prices) {
        BigDecimal low = prices.low();
        BigDecimal high = prices.high();
        BigDecimal close = prices.close();
        BigDecimal ts = high.subtract(low);
        BigDecimal ys = this.prevClose == null ? BigDecimal.ZERO : high.subtract(this.prevClose);
        BigDecimal yst = this.prevClose == null ? BigDecimal.ZERO : this.prevClose.subtract(low);
        this.prevClose = close;
        return ts.abs().max(ys.abs()).max(yst.abs());
    }
}