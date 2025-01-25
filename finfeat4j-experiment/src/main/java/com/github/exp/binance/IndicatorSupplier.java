package com.github.exp.binance;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.helpers.bar.Close;
import com.github.finfeat4j.others.quant.QuantIndicator;
import com.github.finfeat4j.ta.ARSI;
import com.github.finfeat4j.ta.MFI;
import com.github.finfeat4j.ta.NET;
import com.github.finfeat4j.ta.WLR;

import java.math.BigDecimal;

public class IndicatorSupplier {
    public static IndicatorSet<Bar> get() {
        int winSize = 32;
        // just for testing purpose, any other combination of indicators can be used
        var indicatorSet = new IndicatorSet<Bar>();
        for (int i = 0; i < 31; i+=2) {
            //   indicatorSet.add(new Close().then(new QuantIndicator(16, 6, 2)));
            //indicatorSet.add(new PGO(i + 2, new Close()).then(new QuantIndicator(16)));
            // for (int j = 0; j <= 4; j+=2) {
            for (int s = 3; s <= 12; s += 4) {
                indicatorSet.add(toQuant(new WLR(i + 2).then(new NET(s))));
                indicatorSet.add(toQuant(new Close().then(new ARSI(i + 2)).then(new NET(s))));
                //  indicatorSet.add(toQuant(new Close().then(new IFTRSI(i + 2, s))));
                indicatorSet.add(toQuant(new MFI(i + 2).then(new NET(s))));
                // indicatorSet.add(toQuant(new Close().then(new TSI(i + 2)).then(new PFE(s, 3))));
                // indicatorSet.add(toQuant(new Close().then(new RSI(i + 2)).then(new PFE(s, 3))));
            }
            //  }
            // indicatorSet.add(new Close().then(new RSI(i + 2)).then(new QuantIndicator(16, 6, 2)));
            //    indicatorSet.add(new Close().then(new VST(i + 2)).then(new QuantIndicator(16, 6, 2)));
            for (int j = 0; j < 4; j++) {
                // indicatorSet.add(new VAMA(i + 2).then(new RSI(j + 2)).then(new QuantIndicator(16, 6, 2)));
            }
            //indicatorSet.add(new MFI(i + 2).then(new QuantIndicator(16)));
            //indicatorSet.add(new Close().then(new DPO(i + 2)).then(new QuantIndicator(16)));
            /*indicatorSet.add(new Close().then(new Fisher(i + 2)).then(new QuantIndicator(16)));
            for (int j = 0; j < 12; j+=2) {
                indicatorSet.add(new Close().then(new Fisher(i + 2)).then(new QuantIndicator(16)));
            }*/
        }

        for (int i = 5; i < 10; i+= 5) {
            for (int slow = 10; slow < 30; slow+=5) {
                for (int fast = 5; fast < 20; fast+=5) {
                //    indicatorSet.add(new Close().then(new CRSI(slow, i, fast)).then(new QuantIndicator(16, 4, 2)));
                    // indicatorSet.add(new Close().then(new STC(i, fast, slow, 0.5d)).then(new QuantIndicator(16, 6, 2)));
                }
            }
        }
        /*IntStream.of(2, 3, 5, 8, 13, 21, 32).forEach(i -> {
            IntStream.of(2, 3, 4).forEach(b -> {
                IntStream.of(5, 10, 15).forEach(c -> {
                    indicatorSet.add(new MACDP<>(new Close().then(new BB.UB(new SMA(i), b, c)), new Close()).then(new QuantIndicator(16, 6, 2)));
                    indicatorSet.add(new MACDP<>(new Close().then(new BB.LB(new SMA(i), b, c)), new Close()).then(new QuantIndicator(16, 6, 2)));
                });
            });
        });*/

        // indicatorSet.add(new OBV().then(new QuantIndicator(16)));
        indicatorSet.add(new Close().rename("trendPrice"));
        indicatorSet.add(new Close().rename("price"));
        indicatorSet.add(Indicator.of((b) -> -1, "class"));
        return indicatorSet;
    }

    private static <T> Indicator<T, double[]> toQuant(Indicator<T, BigDecimal> indicator) {
        return indicator.then(new QuantIndicator(16, 4, 2));
    }
}
