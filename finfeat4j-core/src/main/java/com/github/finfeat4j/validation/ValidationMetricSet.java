package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.label.Instance;

import java.util.ArrayList;
import java.util.List;

public class ValidationMetricSet extends IndicatorSet<Instance> {

    private final List<Double> worstObjectives = new ArrayList<>();
    private final List<Boolean> maximize = new ArrayList<>();

    public ValidationMetricSet() {
        add(new Accuracy(), new Kappa(), new F1Score(), new MCC(), new BrierScore(), new CrossEntropy(), new MDL(), new BIC(), new AIC(), new Error());
    }

    @Override
    protected void onAdd(Indicator<Instance, ?> indicator) {
        if (indicator instanceof Classifier.ValidationMetric<?> metric) {
            super.onAdd(indicator);
            for (boolean b : metric.maximize()) {
                this.maximize.add(b);
                if (b) {
                    this.worstObjectives.add(Double.NEGATIVE_INFINITY);
                } else {
                    this.worstObjectives.add(Double.POSITIVE_INFINITY);
                }
            }
        } else {
            throw new IllegalArgumentException("Metric must be an instance of OnlineMetric");
        }
    }

    public double[] worstObjectives() {
        return worstObjectives.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public boolean[] maximize() {
        var maximize = new boolean[this.maximize.size()];
        for (int i = 0; i < maximize.length; i++) {
            maximize[i] = this.maximize.get(i);
        }
        return maximize;
    }
}
