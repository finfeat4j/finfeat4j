package com.github.finfeat4j.fs.jmetal;

import com.github.finfeat4j.fs.Metric;
import com.github.finfeat4j.util.Dataset;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class BaseFeatureSelectionModel<T> implements FeatureSelectionModel<T> {

    private final List<Metric<T>> metrics;
    private final boolean[] maximize;
    protected final Dataset dataset;
    protected final double[] worstObjectives;
    private final Set<String> skipFeatures = Set.of("class", "price", "trendPrice");
    private final Map<Integer, double[]> resultsCache = new ConcurrentHashMap<>();
    private final int metricSize;

    public BaseFeatureSelectionModel(Dataset dataset, List<Metric<T>> metrics) {
        this.dataset = dataset;
        this.metricSize = metrics.size();
        this.metrics = Stream.of(metrics.stream(), allMetrics().stream()).flatMap(Function.identity()).distinct().toList();
        this.maximize = new boolean[this.metrics.size()];
        this.worstObjectives = new double[this.metrics.size()];
        for (int i = 0; i < this.maximize.length; i++) {
            this.maximize[i] = this.metrics.get(i).maximize();
        }
        for (int i = 0; i < this.worstObjectives.length; i++) {
            if (this.maximize[i]) {
                this.worstObjectives[i] = Double.NEGATIVE_INFINITY;
            } else {
                this.worstObjectives[i] = Double.POSITIVE_INFINITY;
            }
        }
    }

    @Override
    public double[] fitness(String[] features) {
        if (features.length < 3) {
            return this.worstObjectives.clone();
        }
        int key = Set.of(features).hashCode();
        if (resultsCache.containsKey(key)) {
            return resultsCache.get(key);
        }
        var fitness = calculateFitness(features);
        var result = metrics.stream()
                .mapToDouble(metric -> metric.apply(fitness))
                .toArray();
        resultsCache.put(key, result);
        return result;
    }

    protected abstract T calculateFitness(String[] features);

    @Override
    public List<Metric<T>> metrics() {
        return this.metrics;
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
    public double[] worstObjectives() {
        return this.worstObjectives;
    }

    @Override
    public Set<String> skipFeatures() {
        return this.skipFeatures;
    }
}
