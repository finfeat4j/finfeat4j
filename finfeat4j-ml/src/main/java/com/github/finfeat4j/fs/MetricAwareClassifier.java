package com.github.finfeat4j.fs;

import com.github.finfeat4j.label.Instance;
import com.github.finfeat4j.validation.ValidationMetricSet;
import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.validation.TradingMetrics;
import com.github.finfeat4j.validation.TradingEngine;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Wraps any strategy, computes and makes metrics accessible
 */
public class MetricAwareClassifier implements Classifier {
    private final Function<Instance, double[]> transform;
    private final Classifier strategy;
    private final TradingEngine engine;

    private double[] metrics;
    private TradingEngine.Result lastTradeMetric;
    private final String[] metricNames;
    private final boolean[] maximize;
    private final double[] worstObjectives;

    public MetricAwareClassifier(Classifier strategy, double fee, ValidationMetricSet metrics) {
        this.engine = new TradingEngine(fee);
        this.strategy = strategy;
        metrics.add(new TradingMetrics(this.engine));
        this.transform = metrics
                .transform();
        this.metricNames = metrics.names();
        this.maximize = metrics.maximize();
        this.worstObjectives = metrics.worstObjectives();
    }

    @Override
    public Instance[] fit(TrainTest trainTest) {
        return updateMetrics(strategy.fit(trainTest));
    }

    @Override
    public Instance[] predict(TrainTest trainTest) {
        return updateMetrics(strategy.predict(trainTest));
    }

    private Instance[] updateMetrics(Instance[] predictions) {
        this.metrics = Arrays.stream(predictions)
            .map(transform)
            .reduce((a, b) -> b)
            .orElseThrow();
        this.lastTradeMetric = engine.last();
        return predictions;
    }

    public TradingEngine.Result lastTradeMetric() {
        return lastTradeMetric;
    }

    public double[] metrics() {
        return metrics;
    }

    public String[] metricNames() {
        return metricNames;
    }

    public boolean[] maximize() {
        return maximize;
    }

    public double[] worstObjectives() {
        return worstObjectives;
    }
}
