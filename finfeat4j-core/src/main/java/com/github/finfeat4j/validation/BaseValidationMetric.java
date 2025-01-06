package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.label.Instance;

public abstract class BaseValidationMetric<T> implements Classifier.ValidationMetric<T> {
    private T last;
    private long lastTimestamp = Long.MIN_VALUE;

    @Override
    public T last() {
        return last;
    }

    @Override
    public void setLast(T last) {
        this.last = last;
    }

    @Override
    public boolean shouldSkip(Instance prediction) {
        var shouldSkip = Classifier.ValidationMetric.super.shouldSkip(prediction);
        if (!shouldSkip) {
            shouldSkip = prediction.timestamp() < lastTimestamp;
            this.lastTimestamp = !shouldSkip ? prediction.timestamp() : lastTimestamp;
        }
        return shouldSkip;
    }
}
