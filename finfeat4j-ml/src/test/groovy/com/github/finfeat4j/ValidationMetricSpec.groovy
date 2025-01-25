package com.github.finfeat4j

import com.github.finfeat4j.api.LabelProducer
import com.github.finfeat4j.label.Instance
import com.github.finfeat4j.validation.CrossEntropy
import smile.validation.metric.LogLoss

import java.util.stream.IntStream

class ValidationMetricSpec extends BaseSpec {

    def 'test cross entropy'() {
        when:
        int[] y = [1, 0, 1, 1, 0, 1, 0, 1, 0, 1]
        double[] p = [0.9, 0.1, 0.8, 0.85, 0.2, 0.75, 0.3, 0.9, 0.4, 0.6]

        var a = LogLoss.of(y, p)

        long[] timestamp = new long[1]

        var b = IntStream.range(0, y.length).mapToObj(i -> {
            def instance = new Instance(0.0, 0.0, 1.0, timestamp[0]++, null)
            def probs = new double[2];
            probs[y[i]] = p[i]
            probs[1 - y[i]] = 1 - p[i]
            instance.setProbabilities(probs)
            instance.setActual(LabelProducer.Label.valueOf(y[i]));
            return instance
        }).map(new CrossEntropy())
        .reduce((c, b) -> b)
        .get();



        println a
        println b

        then:

        true
    }
}
