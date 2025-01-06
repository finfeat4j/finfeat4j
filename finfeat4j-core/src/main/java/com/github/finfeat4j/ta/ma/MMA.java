package com.github.finfeat4j.ta.ma;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

import static com.github.finfeat4j.util.Util.ema;

/**
 * Modified Moving Average (MMA).
 */
public class MMA implements Indicator<BigDecimal, BigDecimal> {

    private final Params params;

    private final double ratio;
    private BigDecimal ema;

    public MMA(int length) {
        this.ratio = 1.0 / length;
        this.params = new Params(length);
    }

    public MMA(double ratio, int length) {
        this.ratio = ratio;
        this.params = new Params(length);
    }

    @Override
    public BigDecimal apply(BigDecimal v) {
        if (this.ema == null) {
            this.ema = v;
        } else {
            this.ema = round(ema(v.doubleValue(), this.ema.doubleValue(), ratio), v);
        }
        return this.ema;
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
