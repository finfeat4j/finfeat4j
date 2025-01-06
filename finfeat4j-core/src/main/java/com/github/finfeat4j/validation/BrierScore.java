package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class BrierScore extends BaseValidationMetric<Double> {
    private double cumulativeScore = 0.0;
    private int totalSamples = 0;

    @Override
    public Double compute(Instance prediction) {
        totalSamples++;
        double[] probabilities = prediction.probabilities();

        if (probabilities == null || probabilities.length == 0) {
            throw new IllegalArgumentException("Prediction probabilities must not be null or empty.");
        }

        int actual = prediction.actual().code();
        if (actual < 0 || actual >= probabilities.length) {
            throw new IllegalArgumentException(
                "Actual value must correspond to a valid index in the probabilities array."
            );
        }

        // Compute Brier score for the current instance
        double instanceScore = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            double observed = (i == actual) ? 1.0 : 0.0;
            instanceScore += Math.pow(probabilities[i] - observed, 2);
        }

        cumulativeScore += instanceScore;

        // Return the average Brier score so far
        return round(cumulativeScore / totalSamples);
    }

    @Override
    public boolean[] maximize() {
        return new boolean[] {false}; // Brier Score is minimized
    }
}