package com.github.finfeat4j.fs.smile;

import com.github.finfeat4j.fs.Metric;
import com.github.finfeat4j.fs.jmetal.BaseFeatureSelectionModel;
import com.github.finfeat4j.label.LabelProducer;
import com.github.finfeat4j.trading.TradingEngine;
import com.github.finfeat4j.util.Dataset;
import smile.classification.Classifier;
import smile.validation.metric.Accuracy;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class SmileFeatureSelectionModel<T> extends BaseFeatureSelectionModel<double[]> {

    public enum SmileMetric implements Metric<double[]> {
        ACCURACY(true, (d) -> d[0]),
        FEATURE_COUNT(false, (d) -> d[1]),
        PROFIT(true, (d) -> d[2]),
        MAX_DRAWDOWN(false, (d) -> d[3]);

        private final boolean maximize;
        private final Function<double[], Double> func;

        SmileMetric(boolean maximize, Function<double[], Double> func) {
            this.maximize = maximize;
            this.func = func;
        }


        @Override
        public boolean maximize() {
            return this.maximize;
        }

        @Override
        public Double apply(double[] doubles) {
            return func.apply(doubles);
        }
    }

    private final Function<SplitSet, Classifier<T>> fit;
    private final BiFunction<SplitSet, Classifier<T>, int[]> predict;
    private final double trainRatio;
    private final int[] classColumn;
    private final double[] priceColumn; // todo add implementation
    private final double[] trendPriceColumn; // todo add implementation
    private final Supplier<TradingEngine> tradingEngine;

    public SmileFeatureSelectionModel(Function<SplitSet, Classifier<T>> fit,
                                      BiFunction<SplitSet, Classifier<T>, int[]> predict, Dataset dataset, double trainRatio, Supplier<TradingEngine> tradingEngine) {
        super(dataset, List.of(SmileMetric.ACCURACY, SmileMetric.PROFIT));
        this.fit = fit;
        this.tradingEngine = tradingEngine;
        this.predict = predict;
        this.trainRatio = trainRatio;
        this.classColumn = Arrays.stream(dataset.column("class")).mapToInt(x -> (int) x).toArray();
        this.priceColumn = dataset.column("price");
        this.trendPriceColumn = dataset.column("trendPrice");
    }

    @Override
    protected double[] calculateFitness(String[] features) {
        var skipFeatures = this.skipFeatures();
        // todo: this can be a stream, no need to produce reduced dataset here
        var selected = this.dataset
                .select(Arrays.stream(features).filter(Predicate.not(skipFeatures::contains)).distinct().toArray(String[]::new))
                .data();
        var trainIdx = (int) (this.dataset.data().length * trainRatio);
        var trainSet = new SplitSet(Arrays.stream(selected).limit(trainIdx), Arrays.stream(this.classColumn).limit(trainIdx));
        var testSet = new SplitSet(Arrays.stream(selected).skip(trainIdx), Arrays.stream(this.classColumn).skip(trainIdx));
        var classifier = this.fit.apply(trainSet);
        var predictions = this.predict.apply(testSet, classifier);
        var tradingEngine = this.tradingEngine.get();
        var tradeResult = IntStream.range(0, predictions.length).mapToObj(i ->
            tradingEngine.apply(new LabelProducer.Instance(this.priceColumn[trainIdx + i], LabelProducer.Label.valueOf(predictions[i]), i + 1))
        ).reduce((first, second) -> second).orElseThrow();
        return new double[]{Accuracy.of(Arrays.stream(this.classColumn).skip(trainIdx).toArray(), predictions), features.length, tradeResult.totalProfit(), tradeResult.maxDrawdown()};
    }

    @Override
    public List<Metric<double[]>> allMetrics() {
        return List.of(SmileMetric.ACCURACY, SmileMetric.FEATURE_COUNT, SmileMetric.PROFIT, SmileMetric.MAX_DRAWDOWN);
    }
}
