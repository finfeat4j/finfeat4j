package com.github.finfeat4j.fs

import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.DirectionalChange
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.ml.sfa.SFATransformers
import com.github.finfeat4j.others.quant.QuantIndicator
import com.github.finfeat4j.stats.PCC
import com.github.finfeat4j.ta.ARSI
import com.github.finfeat4j.ta.BB
import com.github.finfeat4j.ta.CRSI
import com.github.finfeat4j.ta.DPO
import com.github.finfeat4j.ta.IFTRSI
import com.github.finfeat4j.ta.MACDP
import com.github.finfeat4j.ta.MFI
import com.github.finfeat4j.ta.OBV
import com.github.finfeat4j.ta.PFE
import com.github.finfeat4j.ta.PGO
import com.github.finfeat4j.ta.RSI
import com.github.finfeat4j.ta.STC
import com.github.finfeat4j.ta.USI
import com.github.finfeat4j.core.Dataset
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.ta.VSCT
import com.github.finfeat4j.ta.VST
import com.github.finfeat4j.ta.WLR
import com.github.finfeat4j.ta.ma.SMA
import com.github.finfeat4j.ta.ma.US
import com.github.finfeat4j.ta.ma.VAMA
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sfa.transformation.SFA

import java.util.function.Supplier
import java.util.stream.Stream

class DatasetSupplier implements Supplier<Dataset> {
    private static final Logger log = LoggerFactory.getLogger(DatasetSupplier.class)
    private final Supplier<Stream<Bar>> stream;
    private final File modelFile;
    private final boolean createModel;

    DatasetSupplier(Supplier<Stream<Bar>> stream, File modelFile, boolean createModel) {
        this.stream = stream
        this.modelFile = modelFile;
        this.createModel = createModel;
    }

    public IndicatorSet<Bar> getIndicatorSet() {
        int winSize = 32;
        // just for testing purpose, any other combination of indicators can be used
        var indicatorSet = new IndicatorSet<Bar>();
        for (int i = 0; i < 31; i+=2) {
            //   indicatorSet.add(new Close().then(new QuantIndicator(16, 6, 2)));
            //indicatorSet.add(new PGO(i + 2, new Close()).then(new QuantIndicator(16)));
            // for (int j = 0; j <= 4; j+=2) {
            indicatorSet.add(new MFI(i + 2).then(new QuantIndicator(16, 4, 2)));
            indicatorSet.add(new Close().then(new ARSI(i + 2)).then(new QuantIndicator(16, 4, 2)));
            indicatorSet.add(new Close().then(new VSCT(i + 2)).then(new QuantIndicator(16, 4, 2)));
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
                    indicatorSet.add(new Close().then(new CRSI(slow, i, fast)).then(new QuantIndicator(16, 4, 2)));
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

    @Override
    Dataset get() {
        // just for testing purpose, any other combination of indicators can be used
        var winSize = 32;
        var indicatorSet = getIndicatorSet();
        var withLabels = indicatorSet.withLabels(stream.get(), new DirectionalChange(0.05), stream.get().count())
        SFATransformers transformers
        if (!createModel) {
            transformers = SFATransformers.load(modelFile)
        } else {
            transformers = SFATransformers.fit(withLabels,
                    SFA.HistogramType.EQUI_DEPTH, 8, 2, winSize, modelFile, "class",
                    0.5d, "trendPrice", "price", "class"
            )
        }
        return transformers.asIndicatorSet(withLabels.features())
            .asDataset(Arrays.stream(withLabels.data()), winSize)
    }
}
