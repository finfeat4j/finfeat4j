package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;

import java.math.BigDecimal;

public class MACDP<A> implements Indicator<A, BigDecimal> {

    private final Indicator<A, BigDecimal> fast;
    private final Indicator<A, BigDecimal> slow;
    private final Params params;

    public MACDP(Indicator<A, BigDecimal> fast, Indicator<A, BigDecimal> slow) {
        this.fast = fast;
        this.slow = slow;
        this.params = new Params(fast.getName(), slow.getName());
    }

    @Override
    public BigDecimal apply(A a) {
        var shortEma = this.fast.apply(a).doubleValue();
        var longEma = this.slow.apply(a).doubleValue();
        double val = ((longEma - shortEma) / shortEma) * 100;
        return round(val, 2);
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
