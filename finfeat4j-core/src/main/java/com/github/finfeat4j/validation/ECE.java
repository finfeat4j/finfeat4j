package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

/**
 * Expected Calibration Error (ECE) is a metric that measures the difference between the predicted
 * probabilities and the true accuracy of a model.
 */
public class ECE extends BaseValidationMetric<Double> {
    private int totalSamples = 0;
    private final int numBins = 10;
    private final int[] binCounts;
    private final double[] binAccuracy;
    private final double[] binConfidence;

    public ECE() {
        binCounts = new int[numBins];
        binAccuracy = new double[numBins];
        binConfidence = new double[numBins];
    }

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

        // Find the bin for the predicted probability
        double predictedProb = probabilities[actual];
        int binIndex = (int) (predictedProb * numBins);
        binIndex = Math.min(binIndex, numBins - 1); // Ensure it's within bounds

        binCounts[binIndex]++;
        binAccuracy[binIndex] += (actual == prediction.predicted().code()) ? 1 : 0;
        binConfidence[binIndex] += predictedProb;

        double ece = 0.0;
        for (int i = 0; i < numBins; i++) {
            if (binCounts[i] > 0) {
                double accuracy = binAccuracy[i] / binCounts[i];
                double confidence = binConfidence[i] / binCounts[i];
                ece += (Math.abs(accuracy - confidence) * binCounts[i]) / totalSamples;
            }
        }

        return round(ece);
    }

    @Override
    public boolean[] maximize() {
        return new boolean[] {false}; // ECE is minimized
    }
}