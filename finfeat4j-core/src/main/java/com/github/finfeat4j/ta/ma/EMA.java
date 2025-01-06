package com.github.finfeat4j.ta.ma;

/**
 * Exponential Moving Average (EMA).
 */
public class EMA extends MMA {
    public EMA(int length) {
        super(2.0 / ((double) (length + 1)), length);
    }
}