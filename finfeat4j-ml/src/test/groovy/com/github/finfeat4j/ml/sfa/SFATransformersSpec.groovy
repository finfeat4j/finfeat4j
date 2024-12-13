package com.github.finfeat4j.ml.sfa

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.core.Bar
import com.github.finfeat4j.core.Indicator
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.InstanceTransformer
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.ma.SMA
import com.github.finfeat4j.others.quant.QuantIndicator
import com.github.finfeat4j.util.IndicatorSet
import sfa.transformation.SFA

import java.util.function.Supplier

class SFATransformersSpec extends BaseSpec {

    def 'test fit and transform'() {
        given:
        def winSize = 16
        def modelFile = File.createTempFile("sfa", "ser")
        def stream = BARS.stream()
        def indicators = new IndicatorSet<>(
            // start features
            new Close().then(new SMA(3)),
            new Close().then(new SMA(5)),
            new Close().then(new SMA(6)),
            // end features
            new Close().rename("trendPrice"), // price on which labels are calculated
            new Close().rename("price"), // price
            Indicator.of((b) -> -1, "class") // placeholder for label
        )

        when:
        def withLabels = indicators.withLabels(stream, new TrendLabel(0.05), BARS.size())
        def transformers = SFATransformers.fit(withLabels,
            SFA.HistogramType.EQUI_FREQUENCY, 4, 2, winSize, modelFile, "class",
            0.8d, "trendPrice", "price", "class"
        )

        then: 'result size equals because stopId was used'
        withLabels.data().length == BARS.size()
        withLabels.column("class").every {
            it != -1
        }
        modelFile.exists()
        transformers.sfaMap().size() == 3 // 3 features

        when: 'test sfa transform'
        def sfaTransformed = transformers.asIndicatorSet(withLabels.features())
            .asDataset(Arrays.stream(withLabels.data()), winSize)

        then:
        sfaTransformed.features().length == (3 * 5)
        sfaTransformed.data().length == withLabels.data().length - winSize

        when: 'test sfa transform with loaded model'
        transformers = SFATransformers.load(modelFile.newInputStream())
        def sfaTransformed2 = transformers.asIndicatorSet(withLabels.features())
            .asDataset(Arrays.stream(withLabels.data()), winSize)

        then:
        sfaTransformed.features().every { feature ->
            equals(sfaTransformed.column(feature), sfaTransformed2.column(feature))
        }

        cleanup:
        modelFile.delete()
    }

    /**
     * After feature selection is done you will have to reduce the dataset to the selected features.
     */
    def 'test filter features'() {
        given:
        def winSize = 16
        def modelFile = File.createTempFile("sfa", "ser")
        Supplier<IndicatorSet<Bar>> indicators = () -> new IndicatorSet<Bar>(
                // start features
                new Close().then(new SMA(3)).then(new QuantIndicator(16)),
                new Close().then(new SMA(5)).then(new QuantIndicator(16)),
                new Close().then(new SMA(6)).then(new QuantIndicator(16)),
                // end features
                new Close().rename("trendPrice"), // price on which labels are calculated
                new Close().rename("price"), // price
                Indicator.of((b) -> -1, "class") // placeholder for label
        )

        when:
        def withLabels = indicators.get().withLabels(BARS.stream(), new TrendLabel(0.05), BARS.size())
        def transformers = SFATransformers.fit(withLabels,
                SFA.HistogramType.EQUI_FREQUENCY, 4, 2, winSize, modelFile, "class",
                0.8d, "trendPrice", "price", "class"
        )
        def names = [
                "SFA(QuantIndicator(16,6,4,SMA(3,Close))[0])[1]",
                "SFA(QuantIndicator(16,6,4,SMA(3,Close))[0])[2]",
                "SFA(QuantIndicator(16,6,4,SMA(3,Close))[5])[0]",
                "SFA(QuantIndicator(16,6,4,SMA(5,Close))[2])[0]",
                "trendPrice",
                "price",
                // "class"
        ] as String[]

        def sfaTransformed = transformers.asIndicatorSet(withLabels.features())
                .asDataset(Arrays.stream(withLabels.data()), winSize)
                .select(names)

        def filtered = indicators.get().filter(names)
        def sfaSet = transformers.asIndicatorSet(filtered, names)

        then:
        sfaSet

        when:
        def test = sfaSet.asDataset(filtered.transform(BARS.stream()), winSize);

        then:
        equals(test.features(), sfaTransformed.features())
        for (int i = 0; i < test.data().length; i++) {
            equals(test.data()[i], sfaTransformed.data()[i])
            println "${i} is OK"
        }
    }

}
