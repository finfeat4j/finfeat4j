package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.ta.ma.EMA;

import java.math.BigDecimal;

/**
 * Moving Average Convergence Divergence (MACD) indicator.
 */
public class MACD implements Indicator<BigDecimal, MACD.Result> {
    private final Indicator<BigDecimal, BigDecimal> shortEMA;
    private final Indicator<BigDecimal, BigDecimal> longEMA;
    private final Indicator<BigDecimal, BigDecimal> signalEMA;
    private final Params params;


    public MACD(int shortPeriod, int longPeriod, int signalPeriod) {
        this(new EMA(shortPeriod), new EMA(longPeriod), new EMA(signalPeriod),
                new Params(shortPeriod, longPeriod, signalPeriod));
    }

    public MACD(Indicator<BigDecimal, BigDecimal> shortEMA,
                Indicator<BigDecimal, BigDecimal> longEMA,
                Indicator<BigDecimal, BigDecimal> signalEMA, Params params) {
        this.shortEMA = shortEMA;
        this.longEMA = longEMA;
        this.signalEMA = signalEMA;
        this.params = params;
    }

    @Override
    public Result apply(BigDecimal value) {
        var shortEma = shortEMA.apply(value);
        var longEma = longEMA.apply(value);
        var macd = shortEma.subtract(longEma);
        var signal = signalEMA.apply(macd);
        return new Result(macd, signal, macd.subtract(signal));
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    public record Result(BigDecimal macd, BigDecimal signal, BigDecimal histogram) {
    }
}
