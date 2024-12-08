package com.github.finfeat4j.fs;

import java.util.function.Function;

public interface Metric<T> extends Function<T, Double> {
    boolean maximize();
}
