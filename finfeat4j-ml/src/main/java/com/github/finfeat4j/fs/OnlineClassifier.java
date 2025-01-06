package com.github.finfeat4j.fs;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.validation.ValidationMetricSet;
import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.validation.TradingEngine;
import com.github.finfeat4j.core.IndicatorSet;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class OnlineClassifier {

    private final Supplier<IndicatorSet<Bar>> initial;
    private final Supplier<ValidationMetricSet> metrics;
    private final Supplier<Classifier> strategySupplier;
    private final File sfaModelFile;
    private final String[] features;
    private final double fee;
    private final int trainSize;

    private TrainTestProvider dataProvider;
    private MetricAwareClassifier strategy;

    public OnlineClassifier(
            Supplier<IndicatorSet<Bar>> initial,
            Supplier<ValidationMetricSet> metrics,
            Supplier<Classifier> strategySupplier,
            File sfaModelFile,
            String[] features,
            double fee,
            int trainSize
    ) {
        this.initial = initial;
        this.metrics = metrics;
        this.strategySupplier = strategySupplier;
        this.sfaModelFile = sfaModelFile;
        this.features = features;
        this.fee = fee;
        this.trainSize = trainSize;
    }

    public void init(List<Bar> bars) {
        this.strategy = new MetricAwareClassifier(strategySupplier.get(), fee, metrics.get());
        this.dataProvider = TrainTestProvider.create(initial.get(), sfaModelFile, features, bars, trainSize).get();
        this.strategy.fit(this.dataProvider.get());
    }

    public void update(Bar bar) {
        this.strategy.predict(this.dataProvider.get(bar));
    }

    public double[] getMetrics() {
        return this.strategy.metrics();
    }

    public String[] getMetricNames() {
        return this.strategy.metricNames();
    }

    public TradingEngine.Result getLastTradeMetric() {
        return this.strategy.lastTradeMetric();
    }
}
