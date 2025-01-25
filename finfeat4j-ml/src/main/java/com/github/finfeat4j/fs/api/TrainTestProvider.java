package com.github.finfeat4j.fs.api;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.api.LabelProducer.Label;
import com.github.finfeat4j.core.DoubleDataset;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.label.Instance;
import com.github.finfeat4j.label.InstanceTransformer;
import com.github.finfeat4j.ml.sfa.SFATransformers;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.github.finfeat4j.util.Shuffle.shuffle;

public interface TrainTestProvider extends Supplier<Classifier.TrainTest> {

    Classifier.TrainTest get(Bar bar);

    /**
     * Online version
     *
     * @param initial initial indicators
     * @param sfaModelFile sfa model file
     * @param features indicator features to select
     * @param bars initial bars to train and test on
     * @param trainSize of training data
     * @return TrainTestProvider
     */
    static Supplier<TrainTestProvider> create(IndicatorSet<Bar> initial, LabelProducer labelProducer, boolean useSFA,
                                              File sfaModelFile, String[] features, List<Bar> bars, int trainSize) {
        return () -> {
            final var sfaTransformers = useSFA ? SFATransformers.load(sfaModelFile) : SFATransformers.empty();
            final var indicatorSet = initial.filter(features);
            final var sfaTransformedSet = sfaTransformers.asIndicatorSet(indicatorSet, features);
            final var featureNames = Arrays.asList(sfaTransformedSet.names());
            final int trendPriceIdx = featureNames.indexOf("trendPrice"); // use column config?
            final int priceIdx = featureNames.indexOf("price");
            final int classIdx = featureNames.indexOf("class");
            final var trainTransform = new InstanceTransformer(new LabelProducer.OnlineLabelProducer(labelProducer, bars.size()),
                    trendPriceIdx, priceIdx, classIdx, true);
            final var featureTransform = indicatorSet.transform()
                    .andThen(sfaTransformedSet.transform());

            return new TrainTestProvider() {
                @Override
                public Classifier.TrainTest get() {
                    var initial = bars.stream()
                            .map(featureTransform)
                            .flatMap(t -> trainTransform.apply(t).train())
                            .skip(sfaTransformers.winSize())
                            .toArray(Instance[]::new);
                    var trainStream = Arrays.stream(initial, 0, trainSize);
                    var testStream = Arrays.stream(initial, trainSize, initial.length);
                    return new Classifier.TrainTest(trainStream, trainSize, testStream, initial.length - trainSize, initial[0].x().length);
                }

                @Override
                public Classifier.TrainTest get(Bar bar) {
                    return trainTransform.apply(featureTransform.apply(bar));
                }
            };
        };
    }

    /**
     * Offline version for fast feature selection
     * @param dataset dataset
     * @param folds number of folds
     * @param trainRatio ratio of training data
     * @return function which produces TrainTestProvider array for feature selection
     */
    static Function<String[], TrainTestProvider[]> create(DoubleDataset dataset, int folds, double trainRatio) {
        final var data = dataset.data();
        final var colConfig = dataset.columnConfig();
        final var skipFeatures = List.of(colConfig.price(), colConfig.trendPrice(), colConfig.label());
        final var skipIndexes = dataset.indexOf(skipFeatures);
        final var trainIdx = (int) (data.length * trainRatio);
        record Transform(double[] x, int dataIndex, int index) {};
        Function<Transform, Instance> transform = (t) -> {
            var price = data[t.dataIndex][skipIndexes[0]];
            var trendPrice = data[t.dataIndex][skipIndexes[1]];
            var label = data[t.dataIndex][skipIndexes[2]];
            var instance = new Instance(price, trendPrice, 1.0, t.index, t.x);
            instance.setActual(Label.valueOf((int) label));
            return instance;
        };
        int[][] foldIndexes = new int[folds][];
        foldIndexes[0] = IntStream.range(0, data.length).toArray();
        for (int i = 1; i < folds; i++) {
            foldIndexes[i] = foldIndexes[i - 1].clone();
            shuffle(foldIndexes[i], i);
        }
        var predicate = Predicate.<String>not(skipFeatures::contains);
        return (f) -> {
            var reducer = Indicator.ArrayProducer.defaultReducer(dataset.indexOf(
                Arrays.stream(f).filter(predicate).toArray(String[]::new)
            ));
            var providers = new TrainTestProvider[folds];
            for (int i = 0; i < folds; i++) {
                var fold = foldIndexes[i];
                providers[i] = new TrainTestProvider() {
                    @Override
                    public Classifier.TrainTest get() {
                        var train = IntStream.range(0, trainIdx)
                                        .mapToObj(i -> transform.apply(new Transform(reducer.apply(data[fold[i]]), fold[i], i + 1)));
                        var test = IntStream.range(trainIdx, fold.length)
                                        .mapToObj(i -> transform.apply(new Transform(reducer.apply(data[fold[i]]), fold[i], i + 1)));
                        return new Classifier.TrainTest(train, trainIdx, test, fold.length - trainIdx, f.length);
                    }

                    @Override
                    public Classifier.TrainTest get(Bar bar) {
                        return null;
                    }
                };
            }
            return providers;
        };
    };
}
