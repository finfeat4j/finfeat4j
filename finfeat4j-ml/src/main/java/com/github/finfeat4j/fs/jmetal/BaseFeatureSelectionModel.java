package com.github.finfeat4j.fs.jmetal;

import com.github.finfeat4j.fs.MetricAwareClassifier;
import com.github.finfeat4j.fs.TrainTestProvider;
import com.github.finfeat4j.label.Instance;
import com.github.finfeat4j.validation.TradingEngine;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BaseFeatureSelectionModel implements FeatureSelectionModel {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BaseFeatureSelectionModel.class);

    private final Set<String> skipFeatures = Set.of("class", "price", "trendPrice");
    private final Map<Integer, double[]> resultsCache = new ConcurrentHashMap<>();
    private final int maxFeatures;
    private final int minFeatures;
    private final boolean[] maximize;
    private final double[] worstObjectives;
    private final List<Function<double[], Double>> metricsMapper = new ArrayList<>();
    private final int trainSize;

    private final String[] allMetricsNames;
    private final int metricSize;

    private final Supplier<MetricAwareClassifier> strategySupplier;
    private final Function<String[], TrainTestProvider[]> trainTestProvider;


    public BaseFeatureSelectionModel(int minFeatures, int maxFeatures, String[] optimizationMetrics,
                                     Supplier<MetricAwareClassifier> strategySupplier, Function<String[], TrainTestProvider[]> trainTestProvider) {
        this.minFeatures = minFeatures;
        this.maxFeatures = maxFeatures;
        this.metricSize = optimizationMetrics.length;
        this.strategySupplier = strategySupplier;
        this.trainTestProvider = trainTestProvider;
        var tempStrategy = strategySupplier.get();
        var trainTest = trainTestProvider.apply(new String[0])[0].get();
        this.trainSize = trainTest.trainSize();
        var tradingEngine = new TradingEngine(0.0004, Instance::actual);
        var result = trainTest.test().map(tradingEngine).reduce((a, b) -> b).orElseThrow();
        log.info("TrainSize: {}, Trading engine result: {}", this.trainSize, result);
        var currentNames = List.of(tempStrategy.metricNames());
        this.allMetricsNames = Stream.of(Arrays.stream(optimizationMetrics), Arrays.stream(tempStrategy.metricNames()))
                .flatMap(s -> s)
                .distinct()
                .toArray(String[]::new);
        this.maximize = new boolean[this.allMetricsNames.length];
        this.worstObjectives = new double[this.allMetricsNames.length];
        int i = 0;
        var stMaximize = tempStrategy.maximize();
        var stWorstObjectives = tempStrategy.worstObjectives();
        for (var name : this.allMetricsNames) {
            var index = currentNames.indexOf(name);
            this.metricsMapper.add(d -> d[index]);
            this.maximize[i] = stMaximize[index];
            this.worstObjectives[i] = stWorstObjectives[index];
            i++;
        }
    }

    @Override
    public double[] fitness(String[] features) {
        if (features.length < minFeatures || features.length > maxFeatures) {
            return this.worstObjectives.clone();
        }
        int key = Set.of(features).hashCode();
        return resultsCache.computeIfAbsent(key, k -> {
            var providers = trainTestProvider.apply(features);
            double[][] fitness = new double[providers.length][];
            for (int i = 0; i < providers.length; i++) {
                var strategy = strategySupplier.get();
                try {
                    strategy.fit(providers[i].get());
                } catch (Throwable t) {
                    log.error("Error while fitting strategy with features: " + Arrays.toString(features), t);
                    return worstObjectives.clone();
                }
                fitness[i] = metricsMapper.stream().mapToDouble(m -> m.apply(strategy.metrics())).toArray();
            }
            return Arrays.stream(fitness)
                .reduce(this::reduce)
                .map(avgFitness -> average(avgFitness, providers.length))
                .orElse(fitness[0]);
        });
    }

    private double[] average(double[] fitness, int total) {
        for (int i = 0; i < metricSize; i++) {
            fitness[i] /= total;
        }
        return fitness;
    }

    private double[] reduce(double[] a, double[] b) {
        for (int j = 0; j < metricSize; j++) {
            a[j] += b[j];
        }
        return a;
    }

    @Override
    public int metricSize() {
        return metricSize;
    }

    @Override
    public boolean[] maximize() {
        return maximize;
    }

    @Override
    public String[] metricNames() {
        return this.allMetricsNames;
    }

    @Override
    public Set<String> skipFeatures() {
        return this.skipFeatures;
    }
}
