package com.github.finfeat4j.util;

import com.github.finfeat4j.core.Indicator;

public class FeatureIndex implements Indicator<double[], Double> {

    private final int featureIdx;
    private final String name;

    public FeatureIndex(int featureIdx, String name) {
        this.featureIdx = featureIdx;
        this.name = name;
    }

    @Override
    public Double apply(double[] values) {
        return values[this.featureIdx];
    }

    @Override
    public String getName(Object... attrs) {
        return name;
    }
}