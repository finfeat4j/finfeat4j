package com.github.finfeat4j.trading;

import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.label.LabelProducer;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TradingEngine implements Indicator<LabelProducer.Instance, TradingEngine.Result> {

    private final double fee;
    private Side side;
    private LabelProducer.Instance entryInstance;
    private double totalProfit;
    private double maxDrawdownPercentage;
    private int numberOfTrades;
    private double totalProfitPercentage;

    public TradingEngine(double fee) {
        this.fee = fee;
    }

    @Override
    public Result apply(LabelProducer.Instance instance) {
        updateMaxDrawdown(instance);
        var side = Side.fromLabel(instance.getLabel());
        if (side != null) {
            if (this.side == null) {
                openPosition(side, instance);
            } else if (this.side != side) {
                closePosition(instance);
                openPosition(side, instance);
            }
        }
        return new Result(
                totalProfit,
                Math.abs(maxDrawdownPercentage),
                numberOfTrades > 0 ? totalProfitPercentage / numberOfTrades : 0,
                numberOfTrades
        );
    }

    private void updateMaxDrawdown(LabelProducer.Instance instance) {
        if (this.side == null) {
            return;
        }
        var price = instance.price();
        var entryPrice = this.entryInstance.price();
        var netProfit = getNetProfit(entryPrice, price, 0.0);
        double tradeProfitPercentage = (this.side == Side.LONG)
                ? (netProfit / entryPrice) * 100
                : (netProfit / price) * 100;
        if (tradeProfitPercentage < 0 && tradeProfitPercentage < maxDrawdownPercentage) {
            maxDrawdownPercentage = tradeProfitPercentage;
        }
    }

    private void openPosition(Side side, LabelProducer.Instance instance) {
        this.side = side;
        this.entryInstance = instance;
    }

    private void closePosition(LabelProducer.Instance instance) {
        var price = instance.price();
        var entryPrice = this.entryInstance.price();
        var netProfit = getNetProfit(entryPrice, price, this.fee);

        // Update total profits and metrics.
        totalProfit += netProfit;

        // Calculate profit percentage (positive or negative).
        double tradeProfitPercentage = (this.side == Side.LONG)
                ? (netProfit / entryPrice) * 100
                : (netProfit / price) * 100;
        totalProfitPercentage += tradeProfitPercentage;

        // Increment trade count.
        numberOfTrades++;


        // Reset state for the next trade.
        this.side = null;
        this.entryInstance = null;
    }

    private double getNetProfit(double entryPrice, double price, double fee) {
        var feeEntry = fee * entryPrice; // Entry fee
        var feeExit = fee * price;      // Exit fee

        // Profit calculation for LONG and SHORT trades.
        var profit = this.side == Side.LONG
                ? (price - entryPrice)
                : (entryPrice - price);

        // Adjust profit for fees.
        profit -= (feeEntry + feeExit);

        // Return net profit (can be negative).
        return profit;
    }

    public static Result from(double fee, Stream<LabelProducer.Instance> instances) {
        return instances.map(new TradingEngine(fee)).reduce((first, second) -> second).orElse(null);
    }

    public static Result from(double fee, int[] labels, double[] prices) {
        assert labels.length == prices.length;
        var stream = IntStream.range(0, labels.length)
                .mapToObj(i -> new LabelProducer.Instance(prices[i], LabelProducer.Label.valueOf(labels[i]), i + 1));
        return from(fee, stream);
    }

    public enum Side {
        LONG, SHORT;

        public static Side fromLabel(LabelProducer.Label label) {
            if (label == LabelProducer.Label.BUY) {
                return Side.LONG;
            } else if (label == LabelProducer.Label.SELL) {
                return Side.SHORT;
            }
            return null;
        }
    }

    public record Result(double totalProfit, double maxDrawdown, double averageProfitPercentage, int numberOfTrades) {
    }
}