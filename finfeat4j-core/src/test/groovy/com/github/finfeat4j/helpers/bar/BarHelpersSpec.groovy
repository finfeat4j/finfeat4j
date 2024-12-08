package com.github.finfeat4j.helpers.bar


import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.core.Bar.BaseBar
import spock.lang.Shared

class BarHelpersSpec extends BaseSpec {

    @Shared
    BaseBar bar = new BaseBar(1L, 10.0, 20.0, 30.0, 5.0, 2000.0, 1000L)

    def 'test helpers'() {
        when: 'test Close'
        def test = new Close().apply(bar)

        then:
        test == 20.0

        when: 'test High'
        test = new High().apply(bar)

        then:
        test == 30.0

        when: 'test Low'
        test = new Low().apply(bar)

        then:
        test == 5.0

        when: 'test Open'
        test = new Open().apply(bar)

        then:
        test == 10.0

        when: 'test Volume'
        test = new Vol().apply(bar)

        then:
        test == 2000.0

        when: 'test trades'
        test = new Trades().apply(bar)

        then:
        test == 1000L

        when: 'test AvgOC'
        test = new AvgOC().apply(bar)

        then:
        test == 15.0

        when: 'test AvgPrice'
        test = new AvgPrice().apply(bar)

        then:
        test == 16.2

        when: 'test MedianPrice'
        test = new MedianPrice().apply(bar)

        then:
        test == 17.5

        when: 'test TypicalPrice'
        test = new TypicalPrice().apply(bar)

        then:
        test == 18.3
    }

}
