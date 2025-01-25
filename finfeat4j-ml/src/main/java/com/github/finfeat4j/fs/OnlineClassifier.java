package com.github.finfeat4j.fs;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.fs.api.TrainTestProvider;
import com.github.finfeat4j.validation.TradingEngine;
import com.github.finfeat4j.validation.ValidationMetricSet;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class OnlineClassifier {

    private final Supplier<IndicatorSet<Bar>> initial;
    private final Supplier<ValidationMetricSet> metrics;
    private final Supplier<Classifier> classifierSupplier;
    private final File sfaModelFile;
    private final String[] features;
    private final double fee;
    private final int trainSize;
    private final boolean useSFA;
    private final Supplier<LabelProducer> labelProducer;

    private TrainTestProvider dataProvider;
    private MetricAwareClassifier classifier;

    public OnlineClassifier(
            Supplier<IndicatorSet<Bar>> initial,
            Supplier<ValidationMetricSet> metrics,
            Supplier<Classifier> classifierSupplier,
            Supplier<LabelProducer> labelProducer,
            boolean useSFA,
            File sfaModelFile,
            String[] features,
            double fee,
            int trainSize
    ) {
        this.initial = initial;
        this.metrics = metrics;
        this.classifierSupplier = classifierSupplier;
        this.sfaModelFile = sfaModelFile;
        this.features = features;
        this.fee = fee;
        this.trainSize = trainSize;
        this.useSFA = useSFA;
        this.labelProducer = labelProducer;
    }

    public void init(List<Bar> bars) {
        this.classifier = new MetricAwareClassifier(classifierSupplier.get(), fee, metrics.get());
        this.dataProvider = TrainTestProvider.create(initial.get(), labelProducer.get(), useSFA, sfaModelFile, features, bars, trainSize).get();
        this.classifier.fit(this.dataProvider.get());
    }

    public void update(Bar bar) {
        this.classifier.predict(this.dataProvider.get(bar));
    }

    public double[] getMetrics() {
        return this.classifier.metrics();
    }

    public String[] getMetricNames() {
        return this.classifier.metricNames();
    }

    public TradingEngine.Result getLastTradeMetric() {
        return this.classifier.lastTradeMetric();
    }
}
