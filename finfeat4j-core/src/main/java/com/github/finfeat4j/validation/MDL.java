package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

/**
 * Minimum description length
 */
public class MDL extends BaseValidationMetric<Double> {

    private double encodingLength = 0.0;
    private static final double EPSILON = 1e-15;

    @Override
    public Double compute(Instance prediction) {
        var numParameters = prediction.x().length; // Number of parameters in the model
        var modelComplexity = numParameters * Math.log(prediction.probabilities().length);
        var label = prediction.actual().code();
        var predictedProb = Math.max(EPSILON, Math.min(1 - EPSILON, prediction.probabilities()[label]));
        encodingLength -= Math.log(predictedProb);
        return modelComplexity + encodingLength;
    }

    @Override
    public boolean[] maximize() {
        return MINIMIZE;
    }
}
