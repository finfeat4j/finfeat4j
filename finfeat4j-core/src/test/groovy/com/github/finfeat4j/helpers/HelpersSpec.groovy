package com.github.finfeat4j.helpers

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.helpers.bar.Close

import java.util.function.Supplier

class HelpersSpec extends BaseSpec {

    def 'test ValueBack'() {
        when:
        def test1 = new Close().then(new ValueBack<>(1))
        def test2 = new Close().then(new ValueBack<>(2))
        def test3 = new Close().then(new ValueBack<>(3))
        def test4 = new Close().then(new ValueBack<>(4))
        def stream = BARS.stream().limit(5)
        def set = new IndicatorSet<>(test1, test2, test3, test4).asDataset(stream)
        def close = BARS.stream().limit(5)
                .map(new Close()).mapToDouble(e -> e.doubleValue()).toArray()

        then:
        set.data()[0][0] == close[0]
        set.data()[0][1] == close[0]
        set.data()[0][2] == close[0]
        set.data()[0][3] == close[0]
        //
        set.data()[1][0] == close[0]
        set.data()[1][1] == close[0]
        set.data()[1][2] == close[0]
        set.data()[1][3] == close[0]
        //
        set.data()[2][0] == close[1]
        set.data()[2][1] == close[0]
        set.data()[2][2] == close[0]
        set.data()[2][3] == close[0]
        //
        set.data()[3][0] == close[2]
        set.data()[3][1] == close[1]
        set.data()[3][2] == close[0]
        set.data()[3][3] == close[0]
        //
        set.data()[4][0] == close[3]
        set.data()[4][1] == close[2]
        set.data()[4][2] == close[1]
        set.data()[4][3] == close[0]
    }

    def 'test MinMax'() {
        when:
        def check = load("minmax.csv")
        Supplier<Indicator> wrapper = () ->
                new Indicator.Wrapper<>((e) -> new double[]{e.min(), e.max()}, "")
        def test1 = new Close()
                .then(new MinMax<>(3))
                .then(wrapper.get())
                .array(2);
        def test2 = new Close()
                .then(new MinMax<>(5))
                .then(wrapper.get())
                .array(2);
        def stream = BARS.stream().limit(60)
        // tic()
        def set = new IndicatorSet<>(
            test1,
            test2
        ).asDataset(stream)
        // toc()
        then:
        equals(set, check)
    }

    def 'test Diff'() {
        when:
        def test1 = new Close().then(new Change(1))
        def test2 = new Close().then(new Change(2))
        def test3 = new Close().then(new Change(3))
        def test4 = new Close().then(new Change(4))
        def stream = BARS.stream().limit(5)
        def set = new IndicatorSet<>(test1, test2, test3, test4).asDataset(stream)
        def close = BARS.stream().limit(5)
                .map(new Close()).mapToDouble(e -> e.doubleValue()).toArray()

        then:
        set.data()[0][0] == 0.0d
        set.data()[0][1] == 0.0d
        set.data()[0][2] == 0.0d
        set.data()[0][3] == 0.0d
        //
        set.data()[1][0] == close[1] - close[0]
        set.data()[1][1] == 0.0d
        set.data()[1][2] == 0.0d
        set.data()[1][3] == 0.0d
        //
        set.data()[2][0] == close[2] - close[1]
        set.data()[2][1] == close[2] - close[0]
        set.data()[2][2] == 0.0d
        set.data()[2][3] == 0.0d
        //
        set.data()[3][0] == close[3] - close[2]
        set.data()[3][1] == close[3] - close[1]
        set.data()[3][2] == close[3] - close[0]
        set.data()[3][3] == 0.0d
        //
        set.data()[4][0] == close[4] - close[3]
        set.data()[4][1] == close[4] - close[2]
        set.data()[4][2] == close[4] - close[1]
        set.data()[4][3] == close[4] - close[0]

        and: 'verify names'
        set.features()[0] == "Change(1,Close)"
        set.features()[1] == "Change(2,Close)"
        set.features()[2] == "Change(3,Close)"
        set.features()[3] == "Change(4,Close)"
    }
}
