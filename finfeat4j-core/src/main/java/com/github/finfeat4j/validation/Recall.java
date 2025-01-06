package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class Recall extends BaseValidationMetric<Double> {
    private int tp = 0;
    private int p = 0;

    @Override
    public Double compute(Instance prediction) {
        if (prediction.actual().code() != 0 && prediction.actual().code() != 1) {
            throw new IllegalArgumentException("Recall can only be applied to binary classification: " + prediction.actual().code());
        }

        if (prediction.predicted().code() != 0 && prediction.predicted().code() != 1) {
            throw new IllegalArgumentException("Recall can only be applied to binary classification: " + prediction.predicted().code());
        }

        if (prediction.actual().code() == 1) {
            ++p;
            if (prediction.predicted().code() == 1) {
                ++tp;
            }
        }
        return p != 0 ? (double) tp / p : Double.NEGATIVE_INFINITY;
    }
}
