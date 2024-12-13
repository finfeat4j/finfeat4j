package com.github.finfeat4j.fs

import com.github.finfeat4j.core.Bar
import com.github.finfeat4j.core.Indicator
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.ma.SMA
import com.github.finfeat4j.ml.sfa.SFATransformers
import com.github.finfeat4j.others.quant.QuantIndicator
import com.github.finfeat4j.util.Dataset
import com.github.finfeat4j.util.IndicatorSet
import sfa.transformation.SFA

import java.util.function.Supplier
import java.util.stream.Stream

class DatasetSupplier implements Supplier<Dataset> {
    private final Supplier<Stream<Bar>> stream;
    private final File modelFile = File.createTempFile("sfa", "ser")

    DatasetSupplier(Supplier<Stream<Bar>> stream) {
        this.stream = stream
        this.modelFile.deleteOnExit()
    }

    @Override
    Dataset get() {
        int winSize = 32
        // just for testing purpose, any other combination of indicators can be used
        var indicatorSet = new IndicatorSet<Bar>();
        for (int i = 0; i < 31; i++) {
            indicatorSet.add(new Close().then(new SMA(i + 3)).then(new QuantIndicator(16)))
        }
        indicatorSet.add(new Close().rename("trendPrice"))
        indicatorSet.add(new Close().rename("price"))
        indicatorSet.add(Indicator.of((b) -> -1, "class"))

        var withLabels = indicatorSet.withLabels(stream.get(), new TrendLabel(0.05), stream.get().count())
        var transformers = SFATransformers.fit(withLabels,
                SFA.HistogramType.EQUI_FREQUENCY, 4, 4, winSize, modelFile, "class",
                0.8d, "trendPrice", "price", "class"
        )
        return transformers.asIndicatorSet(withLabels.features())
                .asDataset(Arrays.stream(withLabels.data()), winSize)
    }
}
