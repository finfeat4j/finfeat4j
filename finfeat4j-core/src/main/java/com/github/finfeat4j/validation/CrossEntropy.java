package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

/**
 * CrossEntropy or Log Loss is a metric used to evaluate the performance of a classification model.
 */
public class CrossEntropy extends BaseValidationMetric<Double> {
    private double cumulativeLoss = 0.0;
    private int totalSamples = 0;
    private static final double EPSILON = 1e-15; // To prevent log(0)

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

        // Retrieve the predicted probability for the actual class
        double predictedProb = Math.max(EPSILON, Math.min(1 - EPSILON, probabilities[actual]));

        // Update cumulative loss
        cumulativeLoss -= Math.log(predictedProb);

        return round(cumulativeLoss / totalSamples);
    }

    @Override
    public boolean[] maximize() {
        return MINIMIZE;
    }
}
