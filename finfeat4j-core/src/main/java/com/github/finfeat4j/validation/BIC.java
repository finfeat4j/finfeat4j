package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.label.Instance;

/**
 * Bayesian Information Criterion (BIC) metric.
 */
public class BIC extends BaseValidationMetric<Double> {

    private final Classifier.ValidationMetric<Double> logLikelihood;
    private final Params params;
    private long n;

    public BIC(Classifier.ValidationMetric<Double> logLikelihood) {
        this.logLikelihood = logLikelihood;
        this.params = new Params(logLikelihood.getName());
    }

    @Override
    public Double compute(Instance prediction) {
        var value = Math.log(this.logLikelihood.compute(prediction));
        var numFeatures = prediction.x().length;
        n++;
        return numFeatures * Math.log(n) - 2 * value;
    }

    @Override
    public boolean[] maximize() {
        return new boolean[]{false};
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
