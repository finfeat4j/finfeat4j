package com.github.finfeat4j.validation;

import com.github.finfeat4j.label.Instance;

public class Error extends BaseValidationMetric<Integer> {
    private int incorrect = 0;

    @Override
    public Integer compute(Instance prediction) {
        if (prediction.predicted().code() != prediction.actual().code()) {
            incorrect++;
        }
        return incorrect;
    }

    @Override
    public boolean[] maximize() {
        return MINIMIZE;
    }
}
