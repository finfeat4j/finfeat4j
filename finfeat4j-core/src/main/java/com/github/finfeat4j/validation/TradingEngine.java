package com.github.finfeat4j.validation;

import com.github.finfeat4j.api.LabelProducer.Label;
import com.github.finfeat4j.label.Instance;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TradingEngine extends BaseValidationMetric<TradingEngine.Result> {

    private final double fee;
    private final Function<Instance, Label> func;

    private Side side;
    private Instance entryInstance;
    private double profit;
    private double netProfit;
    private double netLoss;
    private double profitFactor;
    private double maxDrawdownPercentage;
    private int numberOfTrades;
    private double totalProfitPercentage;
    private long lastTimestamp = Long.MIN_VALUE;
    private long totalDuration;

    public TradingEngine(double fee, Function<Instance, Label> func) {
        this.fee = fee;
        this.func = func;
    }

    public TradingEngine(double fee) {
        this(fee, Instance::predicted);
    }

    @Override
    public Result compute(Instance instance) {
        updateMaxDrawdown(instance);
        var side = Side.fromLabel(func.apply(instance));
        if (side != null) {
            if (this.side == null) {
                openPosition(side, instance);
            } else if (this.side != side) {
                closePosition(instance);
                openPosition(side, instance);
            }
        }
        this.lastTimestamp = instance.timestamp();
        return new Result(
                profit,
                numberOfTrades > 1 ? Math.abs(maxDrawdownPercentage) : Double.POSITIVE_INFINITY,
                numberOfTrades > 1 ? totalProfitPercentage / numberOfTrades : Double.NEGATIVE_INFINITY,
                numberOfTrades > 1 ? netProfit : Double.NEGATIVE_INFINITY,
                numberOfTrades > 1 ? netLoss : Double.POSITIVE_INFINITY,
                numberOfTrades > 1 ? profitFactor : Double.NEGATIVE_INFINITY,
                numberOfTrades,
                numberOfTrades > 1 ? (int) (totalDuration / numberOfTrades) : 0,
                position(instance)
        );
    }

    @Override
    public boolean shouldSkip(Instance prediction) {
        return lastTimestamp >= prediction.timestamp();
    }

    public Position position(Instance instance) {
        return new Position(this.side, this.entryInstance, instance, this.fee);
    }

    private void updateMaxDrawdown(Instance instance) {
        if (this.side == null) {
            return;
        }
        var price = instance.price();
        var entryPrice = this.entryInstance.price();
        var tradeProfitPercentage = getNetProfit(this.side, entryPrice, price, 0.0).profitPercentage;
        if (tradeProfitPercentage < 0 && tradeProfitPercentage < maxDrawdownPercentage) {
            maxDrawdownPercentage = tradeProfitPercentage;
        }
    }

    private void openPosition(Side side, Instance instance) {
        this.side = side;
        this.entryInstance = instance;
    }

    private void closePosition(Instance instance) {
        var price = instance.price();
        var entryPrice = this.entryInstance.price();
        var netProfit = getNetProfit(this.side, entryPrice, price, this.fee);
        var profit = netProfit.profit();
        // Update total profits and metrics.
        this.profit += profit;
        if (profit > 0) {
            this.netProfit += profit;
        } else {
            this.netLoss += Math.abs(profit);
        }
        this.profitFactor = this.netLoss > 0 ? this.netProfit / this.netLoss : this.netProfit;
        this.totalDuration += instance.timestamp() - this.entryInstance.timestamp();

        // Calculate profit percentage (positive or negative).
        this.totalProfitPercentage += netProfit.profitPercentage();

        // Increment trade count.
        numberOfTrades++;


        // Reset state for the next trade.
        this.side = null;
        this.entryInstance = null;
    }

    private static Profit getNetProfit(Side side, double entryPrice, double price, double fee) {
        var feeEntry = fee * entryPrice; // Entry fee
        var feeExit = fee * price;      // Exit fee

        // Profit calculation for LONG and SHORT trades.
        var profit = side == Side.LONG
                ? (price - entryPrice)
                : (entryPrice - price);

        // Adjust profit for fees.
        profit -= (feeEntry + feeExit);

        var profitPercentage = (side == Side.LONG)
                ? (profit / entryPrice) * 100
                : (profit / price) * 100;

        // Return net profit (can be negative).
        return new Profit(profit, profitPercentage);
    }

    public static Result from(double fee, Stream<Instance> instances) {
        return instances.map(new TradingEngine(fee)).reduce((first, second) -> second).orElse(null);
    }

    public static Result from(double fee, int[] labels, double[] prices) {
        assert labels.length == prices.length;
        var stream = IntStream.range(0, labels.length)
                .mapToObj(i -> {
                    var label = Label.valueOf(labels[i]);
                    var instance = new Instance(prices[i], prices[i], 1.0d, i + 1, null);
                    instance.setPredicted(label);
                    return instance;
                });
        return from(fee, stream);
    }

    public enum Side {
        LONG, SHORT;

        public static Side fromLabel(Label label) {
            if (label == Label.BUY) {
                return Side.LONG;
            } else if (label == Label.SELL) {
                return Side.SHORT;
            }
            return null;
        }
    }

    public record Profit(double profit, double profitPercentage) {
    }

    public record Position(Side side, double entryPrice, double exitPrice, Profit profit, long duration) {
        public Position(Side side, Instance entry, Instance exit, double fee) {
            this(side, entry.price(), exit.price(), getNetProfit(side, entry.price(), exit.price(), fee), exit.timestamp() - entry.timestamp());;
        }
    }

    public record Result(double totalProfit, double maxDrawdown, double averageProfitPercentage, double netProfit,
                         double netLoss, double profitFactor, int numberOfTrades, int avgBarsInPosition, Position position) {
    }
}