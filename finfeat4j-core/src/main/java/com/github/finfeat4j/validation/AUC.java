package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class AUC extends BaseValidationMetric<Double> {
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
        double auc = (double) tp / (tp + fn) + (double) tn / (tn + fp);
        return round(auc / 2);
    }
}
