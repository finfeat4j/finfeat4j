package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class Precision extends BaseValidationMetric<Double> {
    private int tp = 0;
    private int p = 0;

    @Override
    public Double compute(Instance prediction) {
        if (prediction.predicted().code() != 1 && prediction.predicted().code() != 0) {
            throw new IllegalArgumentException("Predicted value must be 0 or 1");
        }
        if (prediction.actual().code() != 1 && prediction.actual().code() != 0) {
            throw new IllegalArgumentException("Actual value must be 0 or 1");
        }
        if (prediction.predicted().code() == 1) {
            ++p;
            if (prediction.actual().code() == 1) {
                ++tp;
            }
        }
        return p != 0 ? round((double) tp / p) : Double.NEGATIVE_INFINITY;
    }
}
