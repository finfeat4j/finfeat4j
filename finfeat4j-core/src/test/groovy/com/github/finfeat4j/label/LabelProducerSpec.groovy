package com.github.finfeat4j.label

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.api.Classifier
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.ta.ma.SMA
import com.github.finfeat4j.validation.TradingEngine
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.api.LabelProducer.OnlineLabelProducer
import com.github.finfeat4j.api.LabelProducer.Result;

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
        def test = new OnlineLabelProducer(new TrendLabel(0.05))
        def stream = BARS.stream()//.skip(115)
        def set = new IndicatorSet<>(
            new Close().rename("trendPrice"),
            new Close().rename("price"),
            new Close().then(new SMA(3)),
            new Indicator.Wrapper<Bar, Integer>((bar) -> -1, "class"),
        )
        def transformed = set.transform(stream)
            .map(new InstanceTransformer(test, 0, 1, 3))
            .flatMap(Classifier.TrainTest::train)
            .toArray(Instance[]::new)

        then:
        // TODO: add checks
        transformed

        when:
        var prices = Arrays.stream(transformed).mapToDouble(it -> it.price()).toArray()
        var labels = Arrays.stream(transformed).mapToInt(it -> it.actual().code()).toArray()

        var result = TradingEngine.from(0.0004, labels, prices);

        then:
        result
    }
}
