package com.github.finfeat4j.classifier;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.label.Instance;
import smile.classification.DiscreteNaiveBayes;
import smile.classification.KNN;
import smile.math.distance.HammingDistance;
import smile.neighbor.CoverTree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class SmileClassifier<T> implements Classifier {

    public static Function<Classifier.TrainTest, smile.classification.Classifier<int[]>> NB_TRAIN = trainTest -> {
        var ds = IntDataset.from(trainTest.train(), trainTest.trainSize());
        var classifier = new DiscreteNaiveBayes(DiscreteNaiveBayes.Model.POLYAURN, 2, ds.x()[0].length);
        classifier.update(ds.x(), ds.y());
        return classifier;
    };

    public static Function<Classifier.TrainTest, smile.classification.Classifier<int[]>> KNN_TRAIN = trainTest -> {
        var ds = IntDataset.from(trainTest.train(), trainTest.trainSize());
        var tree = CoverTree.of(ds.x(), HammingDistance::d);
        return new KNN<>(tree, ds.y(), 5);
    };

    public static BiFunction<Classifier.TrainTest, smile.classification.Classifier<int[]>, Instance[]> TEST = (trainTest, nb) -> {
        var ds = IntDataset.from(trainTest.test(), trainTest.testSize());
        var probabilities = new double[ds.x().length][2];
        var predictions = nb.predict(ds.x(), probabilities);
        for (int i = 0; i < predictions.length; i++) {
            var instance = ds.instances()[i];
            instance.setPredicted(LabelProducer.Label.valueOf(predictions[i]));
            instance.setProbabilities(probabilities[i]);
        }
        // we can update the model here if needed, but this is skipped now
        // so basically pass train stream as is for metrics calculation
        return Stream.of(Arrays.stream(ds.instances()), trainTest.train())
                .flatMap(i -> i)
                .toArray(Instance[]::new);
    };

    private smile.classification.Classifier<T> classifier;
    private final Function<Classifier.TrainTest, smile.classification.Classifier<T>> train;
    private final BiFunction<Classifier.TrainTest, smile.classification.Classifier<T>, Instance[]> test;

    public SmileClassifier(Function<Classifier.TrainTest, smile.classification.Classifier<T>> train,
                           BiFunction<Classifier.TrainTest, smile.classification.Classifier<T>, Instance[]> test) {
        this.train = train;
        this.test = test;
    }

    @Override
    public Instance[] fit(Classifier.TrainTest trainTest) {
        this.classifier = this.train.apply(trainTest);
        // we do pass empty stream for train here, as we don't need to update the model
        return this.test.apply(new TrainTest(Stream.empty(), -1, trainTest.test(),
                trainTest.testSize(), trainTest.featureSize()), classifier);
    }

    @Override
    public Instance[] predict(Classifier.TrainTest trainTest) {
        return this.test.apply(trainTest, classifier);
    }
}