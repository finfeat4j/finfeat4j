package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.label.Instance;

/**
 * Bayesian Information Criterion (BIC) metric.
 */
public class BIC extends BaseValidationMetric<Double> {

    private final Classifier.ValidationMetric<Double> logLikelihood;
    private long n;

    public BIC() {
        this.logLikelihood = new CrossEntropy();
    }

    @Override
    public Double compute(Instance prediction) {
        // Compute log-likelihood value
        double logLikelihoodValue = this.logLikelihood.compute(prediction);

        // Get the number of parameters/features
        int numFeatures = prediction.x().length;

        // Increment total sample count
        n++;

        // Calculate BIC: ln(n) * k - 2 * log-likelihood
        return Math.log(n) * numFeatures - 2 * logLikelihoodValue;
    }

    @Override
    public boolean[] maximize() {
        return new boolean[]{false};
    }
}
