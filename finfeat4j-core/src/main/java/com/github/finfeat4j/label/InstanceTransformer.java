package com.github.finfeat4j.label;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.api.LabelProducer.OnlineLabelProducer;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Transforms a feature vector into an instance,
 * applies labeling and returns a classifier train-test pair.
 */
public class InstanceTransformer implements Indicator<double[], Classifier.TrainTest> {

    private final OnlineLabelProducer labelProducer;
    private final int trendPriceIdx, priceIdx, classIdx;
    private final boolean remove;
    private long id;

    public InstanceTransformer(OnlineLabelProducer labelProducer, int trendPriceIdx, int priceIdx, int classIdx,
                               boolean remove) {
        this.labelProducer = labelProducer;
        this.trendPriceIdx = trendPriceIdx;
        this.priceIdx = priceIdx;
        this.classIdx = classIdx;
        this.remove = remove;
    }

    public InstanceTransformer(OnlineLabelProducer labelProducer, int trendPriceIdx, int priceIdx, int classIdx) {
        this(labelProducer, trendPriceIdx, priceIdx, classIdx, false);
    }

    @Override
    public Classifier.TrainTest apply(double[] features) {
        var x = features;
        if (remove) {
            x = IntStream.range(0, x.length)
                    .filter(i -> i != trendPriceIdx && i != priceIdx && i != classIdx)
                    .mapToDouble(i -> features[i])
                    .toArray();
        }
        var instance = new Instance(features[priceIdx], features[trendPriceIdx], 1.0d, ++id, x);
        var instances = labelProducer.apply(instance);
        return new Classifier.TrainTest(Stream.of(instances).peek(this::setupClass), instances.length, Stream.of(instance), 1, x.length);
    }

    private void setupClass(Instance instance) {
        if (!remove) {
            instance.x()[classIdx] = instance.actual().code();
        }
    }
}
