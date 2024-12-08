package com.github.finfeat4j.stats;

import com.github.finfeat4j.core.Buffer.DoubleBuffer;
import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.stats.Stats.Result;
import java.math.BigDecimal;

/**
 * Rolling window statistics
 */
public class Stats implements Indicator<BigDecimal, Result> {

    private int n;
    private double K;
    private double Ex;
    private double Ex2;
    private double Ex3;
    private double Ex4;
    private double mad;

    private final DoubleBuffer buffer;
    private final Params params;

    public Stats(int length) {
        this.buffer = new DoubleBuffer(length);
        this.params = new Params(length);
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    @Override
    public Result apply(BigDecimal value) {
        if (value == null) {
            return null;
        }
        var x = value.doubleValue();
        if (this.buffer.isFull()) {
            this.removeValue(this.buffer.head());
        }

        this.buffer.addToEnd(x);
        this.addValue(x);
        return new Result(
            getMean(),
            getVariance(),
            getSkewness(),
            getKurtosis(),
            getStandardDeviation(),
            getMAD()
        );
    }

    public double getVariance() {
        return this.getPopulationVariance();
    }

    public double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
    }

    private void addValue(double value) {
        if (this.n == 0) {
            this.K = value;
        }

        double diff = value - this.K;
        this.Ex += diff;
        this.Ex2 += diff * diff;
        this.Ex3 += diff * diff * diff;
        this.Ex4 += diff * diff * diff * diff;
        ++this.n;
        this.mad += Math.abs(value - this.getMean());
    }

    private void removeValue(double value) {
        double diff = value - this.K;
        this.Ex -= diff;
        this.Ex2 -= diff * diff;
        this.Ex3 -= diff * diff * diff;
        this.Ex4 -= diff * diff * diff * diff;
        --this.n;
        this.mad -= Math.abs(value - getMean());
    }

    public double getMean() {
        return this.K + this.Ex / (double)this.n;
    }

    public double getMAD() {
        return this.mad / this.n;
    }

    private double getPopulationVariance() {
        return (this.Ex2 - this.Ex * this.Ex / (double)this.n) / (double)this.n;
    }

    public double getSkewness() {
        double mean = getMean();
        double stdDev = getStandardDeviation();
        return (this.Ex3 - 3 * mean * this.Ex2 + 2 * mean * mean * this.Ex) / (this.n * stdDev * stdDev * stdDev);
    }

    public double getKurtosis() {
        double mean = getMean();
        double variance = getVariance();
        return (this.Ex4 - 4 * mean * this.Ex3 + 6 * mean * mean * this.Ex2 - 3 * mean * mean * mean * this.Ex) / (this.n * variance * variance) - 3;
    }

    public record Result(double mean, double variance,
                         double skewness, double kurtosis,
                         double stdDev, double mad) {
    }
}
