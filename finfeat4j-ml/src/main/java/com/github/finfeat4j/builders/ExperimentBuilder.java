package com.github.finfeat4j.builders;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.Dataset;
import com.github.finfeat4j.core.DoubleDataset;
import com.github.finfeat4j.fs.BaseFeatureSelectionModel;
import com.github.finfeat4j.fs.MetricAwareClassifier;
import com.github.finfeat4j.fs.api.TrainTestProvider;
import com.github.finfeat4j.validation.ValidationMetricSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExperimentBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExperimentBuilder.class);

    private File featuresFile;
    private File datasetFile;
    private String features;
    private DatasetBuilder datasetBuilder;
    private int minFeaturesInSample = 5;
    private int maxFeaturesInSample = Integer.MAX_VALUE;
    private int newFeaturesPerIteration = 10;
    private double tradingFee = 0.0004;
    private int folds = 1;
    private double trainRatio = 0.8d;
    private String[] optimizeMetrics = new String[] {"MCC", "CrossEntropy"};

    private Supplier<Classifier> classifierSupplier;
    private Supplier<ValidationMetricSet> validationMetricSetSupplier;

    public ExperimentBuilder datasetBuilder(DatasetBuilder datasetBuilder) {
        this.datasetBuilder = datasetBuilder;
        return this;
    }

    public ExperimentBuilder folds(int folds) {
        this.folds = folds;
        return this;
    }

    public ExperimentBuilder trainRatio(double trainRatio) {
        this.trainRatio = trainRatio;
        return this;
    }

    public ExperimentBuilder validationMetricSet(Supplier<ValidationMetricSet> validationMetricSetSupplier) {
        this.validationMetricSetSupplier = validationMetricSetSupplier;
        return this;
    }

    public ExperimentBuilder tradingFee(double tradingFee) {
        this.tradingFee = tradingFee;
        return this;
    }

    public ExperimentBuilder optimizeMetrics(String[] optimizeMetrics) {
        this.optimizeMetrics = optimizeMetrics;
        return this;
    }

    public ExperimentBuilder minFeaturesInSample(int minFeaturesInSample) {
        this.minFeaturesInSample = minFeaturesInSample;
        return this;
    }

    public ExperimentBuilder maxFeaturesInSample(int maxFeaturesInSample) {
        this.maxFeaturesInSample = maxFeaturesInSample;
        return this;
    }

    public ExperimentBuilder newFeaturesPerIteration(int newFeaturesPerIteration) {
        this.newFeaturesPerIteration = newFeaturesPerIteration;
        return this;
    }

    public ExperimentBuilder classifier(Supplier<Classifier> classifierSupplier) {
        this.classifierSupplier = classifierSupplier;
        return this;
    }

    public ExperimentBuilder featuresFile(File featuresFile) {
        this.featuresFile = featuresFile;
        return this;
    }

    public ExperimentBuilder datasetFile(File datasetFile) {
        this.datasetFile = datasetFile;
        return this;
    }

    public ExperimentBuilder features(String features) {
        this.features = features;
        return this;
    }

    public void run() {
        String[][] initial = features != null ? new String[][]{Dataset.features(features, null)} : new String[1][0];
        DoubleDataset dataset = datasetFile != null ? DoubleDataset.load(datasetFile) : datasetBuilder.createDataset();
        if (featuresFile != null) {
            var features = loadText(featuresFile).split("/");
            dataset = (DoubleDataset) dataset.select(Stream.of(Arrays.stream(initial[0]), Arrays.stream(features), Stream.of("price", "trendPrice", "class"))
                    .flatMap(s -> s).distinct().toArray(String[]::new));
        }
        log.info("Starting feature selection");
        Supplier<MetricAwareClassifier> classifierSupplier = () -> new MetricAwareClassifier(this.classifierSupplier.get(), tradingFee, validationMetricSetSupplier.get());
        var model = new BaseFeatureSelectionModel(minFeaturesInSample, maxFeaturesInSample, optimizeMetrics, classifierSupplier, TrainTestProvider.create(dataset, folds,trainRatio));
        model.runAll(dataset.features(), initial, newFeaturesPerIteration);
    }

    private String loadText(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
