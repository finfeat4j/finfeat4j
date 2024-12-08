package com.github.finfeat4j.ma;

import com.github.finfeat4j.core.Indicator;

/**
 * Moving average marker interface.
 */
public interface MovingAverage<T, R> extends Indicator<T, R> {

    static double ema(double value, double ema, double alpha) {
        return (alpha * value) + (1 - alpha) * ema;
    }
}
