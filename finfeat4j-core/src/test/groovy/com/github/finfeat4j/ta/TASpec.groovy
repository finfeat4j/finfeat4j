package com.github.finfeat4j.ta

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.stats.PCC

class TASpec extends BaseSpec {

    def "test RSI"() {
        when:
        def set = new IndicatorSet<>(
            new Close().then(new RSI(3)),
            new Close().then(new RSI(5))
        )

        def dataset = set.asDataset(BARS.stream())

        then:
        dataset
    }

    def "test STC"() {
        when:
        def set = new IndicatorSet<>(
            new Close().then(new STC(12, 20, 10, 0.5)),
            new Close().then(new STC(30, 20, 10, 0.5))
        )

        def dataset = set.asDataset(BARS.stream())

        then:
        dataset
    }

    def "test TSI"() {
        when:
        def set = new IndicatorSet<>(
            new Close().then(new TSI(5)),
            new Close().then(new TSI(10)),
        )

        def dataset = set.asDataset(BARS.stream())

        then:
        dataset
    }

    def 'test ARSI'() {
        when:
        def dataset = new IndicatorSet<>(
            new Close().then(new ARSI(10)),
        ).asDataset(BARS.stream())

        then:
        dataset
    }

    def 'test Fisher'() {
        when:
        def dataset = new IndicatorSet<>(
            new Close().then(new Fisher(11)),
        ).asDataset(BARS.stream())

        then:
        dataset
    }

    def 'test correlation'() {
        when:
        def dataset = new IndicatorSet<>(
            new Close().then(new PCC(10, (val) -> -val)),
            new Close().then(new TSI(10))
        ).asDataset(BARS.stream())

        then:
        dataset
    }
}
