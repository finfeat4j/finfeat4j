package com.github.finfeat4j.ma


import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.util.IndicatorSet

class MASpec extends BaseSpec {

    def 'test SMA'() {
        when:
        def check = load("sma.csv")
        def stream = BARS.stream().limit(12);
        def set = new IndicatorSet<>(
            new Close().then(new SMA(2)),
            new Close().then(new SMA(4)),
            new Close().then(new SMA(6))
        )

        and:
        //tic()
        def sma = set.asDataset(stream)
        //toc()
        def names = set.names()

        then:
        equals(sma, check)
        equals(names, check.features())
    }

    def 'test EMA'() {
        when:
        def check = load("ema.csv")
        def stream = BARS.stream().limit(60)
        def set = new IndicatorSet<>(
            new Close().then(new EMA(10)),
        ).asDataset(stream)

        then:
        equals(set, check)
    }

    def 'test SMM'() {
        when:
        def check = load("smm.csv")
        def stream = BARS.stream().limit(60)
        def set = new IndicatorSet<>(
            new Close().then(new SMM(3)),
            new Close().then(new SMM(5)),
        ).asDataset(stream)

        then:
        equals(set, check)
    }

    def 'test WMA'() {
        when:
        def stream = BARS.stream().limit(60)
        def set = new IndicatorSet<>(
            new Close().then(new WMA(3)),
            new Close().then(new WMA(5)),
        ).asDataset(stream)

        then:
        // TODO: add checks
        set
    }

    def 'test VAMA'() {
        when:
        def stream = BARS.stream().limit(60)
        def set = new IndicatorSet<>(
            new VAMA(3),
            new VAMA(5),
        ).asDataset(stream)

        then:
        // TODO: add checks
        set
    }
}
