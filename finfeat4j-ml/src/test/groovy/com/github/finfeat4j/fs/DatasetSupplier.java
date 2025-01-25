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
        int winSize = 32;
        // just for testing purpose, any other combination of indicators can be used
        var indicatorSet = new IndicatorSet<Bar>();
        //indicatorSet.add(toQuant(new Close()));
        for (int i = 0; i < 31; i+=2) {
            //   indicatorSet.add(new Close().then(new QuantIndicator(16, 6, 2)));
           // indicatorSet.add(toQuant(new PGO(i + 2, new Close())));
            // for (int j = 0; j <= 4; j+=2) {
            //indicatorSet.add(toQuant(new MFI(i + 2)));
            //indicatorSet.add(toQuant(new MFI(i + 2)));
            //indicatorSet.add(toQuant(new Close().then(new ARSI(i + 2))));
          //  indicatorSet.add(toQuant(new Close().then(new VSCT(i + 2))));
           // indicatorSet.add(toQuant(new Close().then(new TSI(i + 2))));
            for (int s = 3; s <= 12; s += 4) {
                indicatorSet.add(toQuant(new WLR(i + 2).then(new NET(s))));
                indicatorSet.add(toQuant(new Close().then(new ARSI(i + 2)).then(new NET(s))));
              //  indicatorSet.add(toQuant(new Close().then(new IFTRSI(i + 2, s))));
                indicatorSet.add(toQuant(new MFI(i + 2).then(new NET(s))));
               // indicatorSet.add(toQuant(new Close().then(new TSI(i + 2)).then(new PFE(s, 3))));
               // indicatorSet.add(toQuant(new Close().then(new RSI(i + 2)).then(new PFE(s, 3))));
            }

            //  }
            //indicatorSet.add(toQuant(new Close().then(new RSI(i + 2))));
            //indicatorSet.add(new Close().then(new VST(i + 2)).then(new QuantIndicator(16, 6, 2)));
            /*for (int j = 0; j < 6; j+=2) {
                indicatorSet.add(toQuant(new Close().then(new USI(i + 2, j + 2))));
                // indicatorSet.add(new VAMA(i + 2).then(new RSI(j + 2)).then(new QuantIndicator(16, 6, 2)));
                indicatorSet.add(toQuant(new MFI(i + 2).then(new PFE(j + 2, 5))));
                indicatorSet.add(toQuant(new Close().then(new ARSI(i + 2)).then(new PFE(j + 2, 5))));
                indicatorSet.add(toQuant(new Close().then(new VSCT(i + 2)).then(new PFE(j + 2, 5))));
                indicatorSet.add(toQuant(new WLR(i + 2).then(new PFE(j + 2, 5))));
            }*/
            //indicatorSet.add(new MFI(i + 2).then(new QuantIndicator(16)));
          //  indicatorSet.add(toQuant(new Close().then(new DPO(i + 2))));
            /*indicatorSet.add(new Close().then(new Fisher(i + 2)).then(new QuantIndicator(16)));
            for (int j = 0; j < 12; j+=2) {
                indicatorSet.add(new Close().then(new Fisher(i + 2)).then(new QuantIndicator(16)));
            }*/
        }

        for (int i = 5; i < 10; i+= 5) {
            for (int slow = 10; slow < 30; slow+=5) {
                for (int fast = 5; fast < 20; fast+=5) {
                   // indicatorSet.add(toQuant(new Close().then(new CRSI(slow, i, fast))));
                    // indicatorSet.add(toQuant(new Close().then(new STC(i, fast, slow, 0.5d))));
                }
            }
        }


        //indicatorSet.add(toQuant(new OBV()));
        indicatorSet.add(new Close().rename("trendPrice"));
        indicatorSet.add(new Close().rename("price"));
        indicatorSet.add(Indicator.of((b) -> -1, "class"));
        return indicatorSet;
    }

    private <T> Indicator<T, double[]> toQuant(Indicator<T, BigDecimal> indicator) {
        return indicator.then(new QuantIndicator(16, 4, 2));
    }
}
