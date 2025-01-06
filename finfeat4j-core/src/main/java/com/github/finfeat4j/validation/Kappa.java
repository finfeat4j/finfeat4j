package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class Kappa extends BaseValidationMetric<Double> {
    private int tp = 0;
    private int fp = 0;
    private int tn = 0;
    private int fn = 0;

    @Override
    public Double compute(Instance prediction) {
        if (prediction.predicted().code() != 1 && prediction.predicted().code() != 0) {
            throw new IllegalArgumentException("Predicted value must be 0 or 1");
        }
        if (prediction.predicted().code() == prediction.actual().code()) {
            if (prediction.predicted().code() == 1) {
                tp++;
            } else {
                tn++;
            }
        } else {
            if (prediction.predicted().code() == 1) {
                fp++;
            } else {
                fn++;
            }
        }
        double c0 = tp + tn + fp + fn;
        if (c0 == 0) {
            return 0.0;
        }
        double p0 = (double) (tp + tn) / c0;
        double pe = (double) ((tp + fp) * (tp + fn) + (fn + tn) * (fp + tn)) / Math.pow(c0, 2);
        double kappa = (p0 - pe) / (1 - pe);
        return round(kappa);
    }
}
