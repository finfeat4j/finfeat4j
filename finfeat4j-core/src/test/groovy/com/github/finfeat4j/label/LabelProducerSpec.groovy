package com.github.finfeat4j.label

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.helpers.bar.AvgPrice
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.ma.SMA
import com.github.finfeat4j.util.IndicatorSet
import com.github.finfeat4j.label.LabelProducer.OnlineLabelProducer;
import com.github.finfeat4j.label.LabelProducer.Instance;
import com.github.finfeat4j.label.LabelProducer.Result;

import java.util.function.Function
import java.util.stream.Stream

class LabelProducerSpec extends BaseSpec {

    def "test TrendLabel producer"() {
        when:
        def test = new TrendLabel(0.05)
        def stream = BARS.stream()
        def labels = stream
            .map(new Close().then(test))
            .map(Stream::of)
            .flatMap(Function.identity())
            .toArray(Result[]::new)

        then:
        // TODO: add checks
        labels
    }

    def 'test DirectionalChange producer'() {
        when:
        def test = new DirectionalChange(0.05)
        def stream = BARS.stream()
        def labels = stream
            .map(new Close().then(test))
            .map(Stream::of)
            .flatMap(it -> it)
            .toArray(Result[]::new)

        then:
        // TODO: add checks
        labels
    }

    def 'test online producer'() {
        when:
        def test = new OnlineLabelProducer(new DirectionalChange(0.05))
        def stream = BARS.stream()
        def set = new IndicatorSet<>(
            new Close(),
            new AvgPrice(),
            new Close().then(new SMA(3))
        )
        long[] id = new long[1];
        def transformed = set.transform(stream)
            .map(new InstanceTransformer(test, 0, 1))
            .flatMap(Function.identity())
            .toArray(Instance[]::new)

        then:
        // TODO: add checks
        transformed
    }
}
