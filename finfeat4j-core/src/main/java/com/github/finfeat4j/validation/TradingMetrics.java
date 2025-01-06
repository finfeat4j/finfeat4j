package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.label.Instance;

public class TradingMetrics extends BaseValidationMetric<double[]>
        implements Indicator.ArrayProducer<Instance, double[]> {

    private static final String[] NAMES = {
        "totalProfit",
        "averageProfitPercentage",
        "profitFactor",
        "numberOfTrades",
        "maxDrawdown",
    };

    private static final boolean[] MAXIMIZE = {
        true,
        true,
        true,
        true,
        false,
    };

    private final TradingEngine engine;

    public TradingMetrics(TradingEngine tradingEngine) {
        this.engine = tradingEngine;
    }

    @Override
    public double[] compute(Instance prediction) {
        return toArray(prediction, engine);
    }

    @Override
    public int size() {
        return NAMES.length;
    }

    @Override
    public String[] arrayNames() {
        return NAMES;
    }

    @Override
    public boolean[] maximize() {
        return MAXIMIZE;
    }

    @Override
    public boolean shouldSkip(Instance prediction) {
        return engine.shouldSkip(prediction);
    }

    private double[] toArray(Instance prediction, TradingEngine engine) {
        return toArray(engine.apply(prediction));
    }

    private double[] toArray(TradingEngine.Result result) {
        return new double[]{
                result.totalProfit(),
                round(result.averageProfitPercentage()),
                round(result.profitFactor()),
                result.numberOfTrades(),
                round(result.maxDrawdown()),
        };
    }
}
