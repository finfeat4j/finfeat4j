package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class F1Score extends BaseValidationMetric<Double> {
    private final Precision precision = new Precision();
    private final Recall recall = new Recall();
    private final double beta = 1.0d;

    @Override
    public Double compute(Instance prediction) {
        var p = precision.apply(prediction);
        var r = recall.apply(prediction);
        var b2 = beta * beta;
        return round((1 + b2) * p * r / (b2 * p + r));
    }
}
