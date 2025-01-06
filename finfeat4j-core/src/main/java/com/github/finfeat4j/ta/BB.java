package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.stats.Stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

public abstract class BB implements Indicator<BigDecimal, BigDecimal> {

    protected final Indicator<BigDecimal, BigDecimal> middle;
    protected final Indicator<BigDecimal, BigDecimal> stdDev;

    protected final BigDecimal k;
    private final Params params;

    protected BB(Indicator<BigDecimal, BigDecimal> middle, int k, int stdDevLen) {
        this.middle = middle;
        this.stdDev = new Stats(stdDevLen).andThen("stdDev", x -> {
            var value = x.stdDev();
            return Double.isNaN(value) || Double.isInfinite(value) ? BigDecimal.ZERO : BigDecimal.valueOf(value);
        });
        this.k = BigDecimal.valueOf(k);
        this.params = new Params(middle.getName(), k, stdDevLen);
    }

    @Override
    public Params getParams() {
        return this.params;
    }

    /**
     * Lower Band (LB) = Middle Band - k * Standard Deviation
     */
    public static class LB extends BB {
        public LB(Indicator<BigDecimal, BigDecimal> middle, int k, int stdDevLen) {
            super(middle, k, stdDevLen);
        }

        @Override
        public BigDecimal apply(BigDecimal x) {
            return this.middle.apply(x).subtract(stdDev.apply(x).multiply(k));
        }
    }

    /**
     * Upper Band (UB) = Middle Band + k * Standard Deviation
     */
    public static class UB extends BB {
        public UB(Indicator<BigDecimal, BigDecimal> middle, int k, int stdDevLen) {
            super(middle, k, stdDevLen);
        }

        @Override
        public BigDecimal apply(BigDecimal x) {
            return this.middle.apply(x).add(stdDev.apply(x).multiply(k));
        }
    }

    public static class Width implements Indicator<BigDecimal, BigDecimal> {
        private final Indicator<BigDecimal, BigDecimal> middle;
        private final UB upperBand;
        private final LB lowerBand;

        private static final BigDecimal HUNDRED = BigDecimal.valueOf(100.0d);

        public Width(Supplier<Indicator<BigDecimal, BigDecimal>> middle, int k, int stdDevLen) {
            this.middle = middle.get();
            this.upperBand = new UB(middle.get(), k, stdDevLen);
            this.lowerBand = new LB(middle.get(), k, stdDevLen);
        }

        @Override
        public BigDecimal apply(BigDecimal x) {
            return upperBand.apply(x).subtract(lowerBand.apply(x)).divide(middle.apply(x), RoundingMode.HALF_UP).multiply(HUNDRED);
        }
    }

    public static class Percentage implements Indicator<BigDecimal, BigDecimal> {
        private final UB upperBand;
        private final LB lowerBand;

        public Percentage(Supplier<Indicator<BigDecimal, BigDecimal>> middle, int k, int stdDevLen) {
            this.upperBand = new UB(middle.get(), k, stdDevLen);
            this.lowerBand = new LB(middle.get(), k, stdDevLen);
        }

        @Override
        public BigDecimal apply(BigDecimal value) {
            var lower = lowerBand.apply(value);
            var upper = upperBand.apply(value);
            if (lower.compareTo(upper) != 0) {
                return value.subtract(lower).divide(upper.subtract(lower), RoundingMode.FLOOR);
            }
            return BigDecimal.ZERO;
        }
    }
}
