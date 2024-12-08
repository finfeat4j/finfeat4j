package com.github.finfeat4j.label;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DirectionalChange implements LabelProducer {
    private final double threshold;
    private final List<Result> reversals = new ArrayList<>();
    private Double extPointN = null;
    private Double currEventMax = null;
    private Double currEventMin = null;
    private int timePointMax = 0;
    private int timePointMin = 0;
    private Label trendStatus = Label.BUY;
    private int T = 0;
    private Double P = null;
    private int time = 0;
    private boolean isLastChangeUp = true;
    private Result lastReversal = null;
    private Result firstOvershoot = null;

    /**
     * @param threshold Change threshold in percentage 0.05 = 5%
     */
    public DirectionalChange(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public Result[] apply(BigDecimal x) {
        return computeDCVariablesStreaming(x.doubleValue());
    }

    private Result[] computeDCVariablesStreaming(double price) {
        this.reversals.clear();
        if (extPointN == null) {
            initializeFirstPrice(price);
        }

        time += 1;

        if (Label.BUY.equals(trendStatus)) {
            processUpwardTrend(price);
        } else {
            processDownwardTrend(price);
        }

        return reversals.toArray(Result[]::new);
    }

    private void initializeFirstPrice(double price) {
        extPointN = price;
        currEventMax = price;
        currEventMin = price;
    }

    private void processUpwardTrend(double price) {
        if (price < ((1 - threshold) * currEventMax)) {
            switchToDownwardTrend(price);
        } else if (price > currEventMax) {
            updateCurrentEvent(price, true);
        }
    }

    private void processDownwardTrend(double price) {
        if (price > ((1 + threshold) * currEventMin)) {
            switchToUpwardTrend(price);
        } else if (price < currEventMin) {
            updateCurrentEvent(price, false);
        }
    }

    private void switchToDownwardTrend(double price) {
        trendStatus = Label.SELL;
        lastReversal = new Result(time - (time - timePointMax), BigDecimal.valueOf(currEventMax), Label.SELL);
        this.firstOvershoot = new Result(T,  BigDecimal.valueOf(P == null ? currEventMin : P), Label.BUY_OVERSHOOT);
        extPointN = currEventMax;
        currEventMin = price;
        T = time;
        P = price;
    }

    private void switchToUpwardTrend(double price) {
        this.trendStatus = Label.BUY;
        this.lastReversal = new Result(time - (time - timePointMin), BigDecimal.valueOf(currEventMin), Label.BUY);
        this.firstOvershoot = new Result(T, BigDecimal.valueOf(P == null ? currEventMax : P), Label.SELL_OVERSHOOT);
        this.extPointN = currEventMin;
        this.currEventMax = price;
        this.T = time;
        this.P = price;
    }

    private void updateCurrentEvent(double price, boolean isMax) {
        if (isMax) {
            currEventMax = price;
            timePointMax = time;
            if (!isLastChangeUp) {
                addReversals();
            }
            isLastChangeUp = true;
        } else {
            currEventMin = price;
            timePointMin = time;
            if (isLastChangeUp) {
                addReversals();
            }
            isLastChangeUp = false;
        }
    }

    private void addReversals() {
        if (lastReversal != null) {
            reversals.add(this.firstOvershoot);
            reversals.add(this.lastReversal);
        }
    }
}
