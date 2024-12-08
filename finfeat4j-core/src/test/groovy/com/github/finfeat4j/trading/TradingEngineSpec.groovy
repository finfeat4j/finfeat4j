package com.github.finfeat4j.trading


import com.github.finfeat4j.BaseSpec


class TradingEngineSpec extends BaseSpec {

    private final double[] prices = [1000, 1001, 1050, 1000, 1001, 1050, 1050]

    def 'test trading engine'() {
        when:
        var labels = [0, 0, 1, 0, 2, 1, 1] as int[]

        var result = TradingEngine.from(0.0, labels, prices)

        then:
        result.numberOfTrades() == 3
        result.totalProfit() == 150.0d
        isCloseTo(result.averageProfitPercentage(), 5.0d)
        result.maxDrawdown() == 0.0d
    }

    def 'test trading engine with fees'() {
        when:
        var labels = [0, 0, 1, 0, 2, 1, 1] as int[]
        var result = TradingEngine.from(0.0004, labels, prices)

        then:
        result.numberOfTrades() == 3
        isCloseTo(result.totalProfit(),  147.54)
        isCloseTo(result.averageProfitPercentage(), 4.918d)
        result.maxDrawdown() == 0.0d
    }

    def 'test trading engine with no trades'() {
        when:
        var labels = [0, 0, 0, 0, 0, 0, 0] as int[]

        var result = TradingEngine.from(0.0, labels, prices)

        then:
        result.numberOfTrades() == 0
        result.totalProfit() == 0.0d
        result.averageProfitPercentage() == 0.0d
        result.maxDrawdown() == 0.0d
    }

    def 'test trading engine with max drawdown'() {
        when:
        var labels = [1, 1, 0, 1, 3, 0, 0] as int[]

        var result = TradingEngine.from(0.0, labels, prices)

        then:
        result.numberOfTrades() == 3
        isCloseTo(result.totalProfit(), -150.0d)
        isCloseTo(result.averageProfitPercentage(), -4.76d)
        isCloseTo(result.maxDrawdown(), -4.76d)
    }
}
