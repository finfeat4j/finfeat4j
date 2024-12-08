package com.github.finfeat4j.label;

import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.label.LabelProducer.Instance;
import com.github.finfeat4j.label.LabelProducer.OnlineLabelProducer;
import java.util.stream.Stream;

public class InstanceTransformer implements Indicator<double[], Stream<Instance>> {

    private final OnlineLabelProducer labelProducer;
    private final int trendPriceIdx, priceIdx;
    private long id;

    public InstanceTransformer(OnlineLabelProducer labelProducer, int trendPriceIdx, int priceIdx) {
        this.labelProducer = labelProducer;
        this.trendPriceIdx = trendPriceIdx;
        this.priceIdx = priceIdx;
    }

    @Override
    public Stream<Instance> apply(double[] features) {
        return Stream.of(this.labelProducer.apply(new Instance(features[priceIdx], features[trendPriceIdx], 1.0d, ++id, features)));
    }
}
