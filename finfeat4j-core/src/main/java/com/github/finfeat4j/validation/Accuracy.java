package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class Accuracy extends BaseValidationMetric<Double> {
    private int correct = 0;
    private int total = 0;

    public Double compute(Instance prediction) {
        if (prediction.predicted().code() == prediction.actual().code()) {
            correct++;
        }
        total++;
        return round((double) correct / total);
    }
}
