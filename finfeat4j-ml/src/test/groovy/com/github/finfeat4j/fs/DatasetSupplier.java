package com.github.finfeat4j.fs;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.helpers.bar.Close;
import com.github.finfeat4j.others.quant.QuantIndicator;
import com.github.finfeat4j.ta.ARSI;
import com.github.finfeat4j.ta.MFI;
import com.github.finfeat4j.ta.NET;
import com.github.finfeat4j.ta.WLR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.function.Supplier;

public class DatasetSupplier implements Supplier<IndicatorSet<Bar>> {
    private static final Logger log = LoggerFactory.getLogger(DatasetSupplier.class);

    public DatasetSupplier() {
    }

    public IndicatorSet<Bar> get() {
        var indicatorSet = new IndicatorSet<Bar>();
        for (int i = 0; i < 31; i+=2) {
            for (int s = 3; s <= 12; s += 4) {
                indicatorSet.add(toQuant(new WLR(i + 2).then(new NET(s))));
                indicatorSet.add(toQuant(new Close().then(new ARSI(i + 2)).then(new NET(s))));
                indicatorSet.add(toQuant(new MFI(i + 2).then(new NET(s))));
            }
        }
        indicatorSet.add(new Close().rename("trendPrice"));
        indicatorSet.add(new Close().rename("price"));
        indicatorSet.add(Indicator.of((b) -> -1, "class"));
        return indicatorSet;
    }

    private <T> Indicator<T, double[]> toQuant(Indicator<T, BigDecimal> indicator) {
        return indicator.then(new QuantIndicator(16, 4, 2));
    }
}
