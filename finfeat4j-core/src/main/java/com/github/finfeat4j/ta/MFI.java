package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.Sum;
import com.github.finfeat4j.helpers.ValueBack;
import com.github.finfeat4j.helpers.bar.TypicalPrice;

import java.math.BigDecimal;

/**
 * Money Flow Index (MFI) indicator.
 */
public class MFI implements Indicator<Bar, BigDecimal> {

    private final Params params;

    private final Sum posBuffer;
    private final Sum negBuffer;
    private final TypicalPrice typicalPrice = new TypicalPrice();
    private final Indicator<Bar, BigDecimal> prevTypicalPrice = new TypicalPrice()
            .then(new ValueBack<>(1));

    public MFI(int length) {
        this.params = new Params(length);
        this.posBuffer = new Sum(length);
        this.negBuffer = new Sum(length);
    }

    @Override
    public BigDecimal apply(Bar bar) {
        var typicalPrice = this.typicalPrice.apply(bar).doubleValue();
        var moneyFlow = typicalPrice * bar.vol().doubleValue();
        var previousTypicalPrice = this.prevTypicalPrice.apply(bar).doubleValue();
        var positiveMoneyFlow = typicalPrice > previousTypicalPrice ? moneyFlow : 0;
        var negativeMoneyFlow = typicalPrice < previousTypicalPrice ? moneyFlow : 0;
        var sumPos = this.posBuffer.apply(positiveMoneyFlow);
        var sumNeg = this.negBuffer.apply(negativeMoneyFlow);
        if (sumNeg.doubleValue() == 0) {
            return BigDecimal.valueOf(100);
        }
        var moneyFlowRatio = sumPos.doubleValue() / sumNeg.doubleValue();
        return BigDecimal.valueOf(100 - 100 / (1 + moneyFlowRatio));
    }

    @Override
    public Params getParams() {
        return this.params;
    }
}
