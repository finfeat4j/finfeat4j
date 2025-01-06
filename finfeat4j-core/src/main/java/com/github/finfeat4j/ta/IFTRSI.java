package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class IFTRSI extends IFT<BigDecimal> {

    private final Params params;

    public IFTRSI(int length, int smooth) {
        super(new RSI(length), smooth, 50);
        this.params = new Params(length, smooth);
    }

    public IFTRSI(int length, Indicator<BigDecimal, BigDecimal> ma) {
        this(new RSI(length), ma);
    }

    public IFTRSI(Indicator<BigDecimal, BigDecimal> indicator, Indicator<BigDecimal, BigDecimal> ma) {
        super(indicator, ma, 50);
        this.params = new Params(indicator.getName(), ma.getName());
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}