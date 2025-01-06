package com.github.finfeat4j.util;

public class Util {
    public static double ema(double value, double ema, double alpha) {
        return (alpha * value) + (1 - alpha) * ema;
    }

}
