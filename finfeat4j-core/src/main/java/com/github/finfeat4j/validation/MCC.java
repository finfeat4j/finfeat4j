package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class MCC extends BaseValidationMetric<Double> {
    private int tp = 0;
    private int fp = 0;
    private int tn = 0;
    private int fn = 0;

    @Override
    public Double compute(Instance prediction) {
        if (prediction.predicted().code() != 1 && prediction.predicted().code() != 0) {
            throw new IllegalArgumentException("Predicted value must be 0 or 1");
        }
        if (prediction.actual().code() != 1 && prediction.actual().code() != 0) {
            throw new IllegalArgumentException("Actual value must be 0 or 1");
        }

        // Update confusion matrix counts
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

        // Compute MCC numerator and denominator
        int numerator = tp * tn - fp * fn;
        double denominator = Math.sqrt(
                (double)(tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)
        );

        // Handle edge case: if denominator is zero, MCC is undefined (return 0.0)
        if (denominator == 0) {
            return 0.0;
        }

        return round(numerator / denominator);
    }
}
