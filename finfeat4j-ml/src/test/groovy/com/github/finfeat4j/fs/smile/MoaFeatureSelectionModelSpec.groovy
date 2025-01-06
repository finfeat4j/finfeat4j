package com.github.finfeat4j.fs.smile

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.fs.DatasetSupplier
import com.github.finfeat4j.fs.MetricAwareClassifier
import com.github.finfeat4j.fs.OnlineClassifier
import com.github.finfeat4j.fs.TrainTestProvider
import com.github.finfeat4j.fs.jmetal.BaseFeatureSelectionModel
import com.github.finfeat4j.fs.jmetal.FeatureSelectionModel
import com.github.finfeat4j.fs.mrmre.MRMRe
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.DirectionalChange
import com.github.finfeat4j.label.Instance
import com.github.finfeat4j.api.LabelProducer
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.validation.ValidationMetricSet
import com.github.finfeat4j.strategy.MoaClassifier
import com.github.finfeat4j.validation.TradingEngine
import com.github.finfeat4j.core.Dataset
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier
import java.util.stream.Stream

class MoaFeatureSelectionModelSpec extends BaseSpec {

    private static final Logger log = LoggerFactory.getLogger(MoaFeatureSelectionModelSpec.class)
    private final File modelFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/rmodel.ser");

    def 'test fit sfa model'() {
        when:
        fitSfaModel(loadBars("data.csv"), modelFile)

        then:
        true
    }

    def 'test moa feature selection' () {
        when:
        log.info("Starting btc test")
        def result = test(loadBars("data.csv"), modelFile, false, "SFA(QuantIndicator(16,4,2,VSCT(12,Close))[75])[2], SFA(QuantIndicator(16,4,2,VSCT(20,Close))[158])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,15,Close))[172])[2], SFA(QuantIndicator(16,4,2,CRSI(10,5,10,Close))[50])[2], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[153])[0], SFA(QuantIndicator(16,4,2,CRSI(25,5,10,Close))[172])[3], SFA(QuantIndicator(16,4,2,ARSI(14,Close))[162])[0], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[75])[0], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[55])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,5,Close))[102])[3], SFA(QuantIndicator(16,4,2,MFI(6))[167])[2], SFA(QuantIndicator(16,4,2,ARSI(2,Close))[128])[3], SFA(QuantIndicator(16,4,2,MFI(28))[146])[0], SFA(QuantIndicator(16,4,2,ARSI(2,Close))[172])[0], SFA(QuantIndicator(16,4,2,ARSI(28,Close))[54])[3], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[0])[3], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[115])[2], SFA(QuantIndicator(16,4,2,MFI(30))[19])[5], SFA(QuantIndicator(16,4,2,VSCT(4,Close))[112])[3], SFA(QuantIndicator(16,4,2,MFI(16))[170])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,10,Close))[7])[0], SFA(QuantIndicator(16,4,2,MFI(14))[110])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,10,Close))[75])[0], SFA(QuantIndicator(16,4,2,ARSI(4,Close))[76])[0], SFA(QuantIndicator(16,4,2,MFI(2))[172])[0], SFA(QuantIndicator(16,4,2,MFI(10))[70])[0], SFA(QuantIndicator(16,4,2,MFI(18))[138])[2], SFA(QuantIndicator(16,4,2,VSCT(6,Close))[102])[3], SFA(QuantIndicator(16,4,2,CRSI(25,5,10,Close))[100])[4], SFA(QuantIndicator(16,4,2,MFI(24))[108])[0], SFA(QuantIndicator(16,4,2,MFI(2))[59])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,15,Close))[70])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,15,Close))[123])[5], SFA(QuantIndicator(16,4,2,MFI(6))[19])[3], SFA(QuantIndicator(16,4,2,MFI(4))[103])[5], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[105])[0], SFA(QuantIndicator(16,4,2,MFI(4))[11])[2], SFA(QuantIndicator(16,4,2,VSCT(6,Close))[15])[5], SFA(QuantIndicator(16,4,2,VSCT(20,Close))[162])[0], SFA(QuantIndicator(16,4,2,MFI(32))[70])[2], SFA(QuantIndicator(16,4,2,ARSI(30,Close))[54])[3], SFA(QuantIndicator(16,4,2,ARSI(10,Close))[109])[3], SFA(QuantIndicator(16,4,2,MFI(12))[129])[3], SFA(QuantIndicator(16,4,2,ARSI(18,Close))[60])[0], SFA(QuantIndicator(16,4,2,VSCT(26,Close))[162])[0], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[158])[5]")
        log.info("Ended btc test")

        then:
        result
    }

    def 'test moa feature selection for eth'() {
        when:
        log.info("Starting eth test")
        def result = test(loadBars("data_eth.csv"), modelFile, false, null)
        log.info("Ended eth test")

        then:
        result

    }

    def 'test moa feature selection for xrp'() {
        when:
        log.info("Starting xrp test")
        def result = test(loadBars("data_xrp.csv"), modelFile, false, null)
        log.info("Ended xrp test")

        then:
        result

    }

    def 'test moa feature selection for bnb'() {
        when:
        log.info("Starting bnb test")
        def result = test(loadBars("data_bnb.csv"), modelFile, false, null)
        log.info("Ended bnb test")

        then:
        result
    }

    def 'test moa feature selection for sol'() {
        when:
        log.info("Starting sol test")
        def result = test(loadBars("data_sol.csv"), modelFile, false, null)
        log.info("Ended sol test")

        then:
        result
    }

    def 'test moa feature selection for doge'() {
        when:
        log.info("Starting doge test")
        def result = test(loadBars("data_doge.csv"), modelFile, false, null)
        log.info("Ended doge test")

        then:
        result
    }

    def 'test moa feature selection for eos'() {
        when:
        log.info("Starting eos test")
        def result = test(loadBars("data_eos.csv"), modelFile, false, null)
        log.info("Ended eos test")

        then:
        result
    }

    def 'test moa feature selection for enj'() {
        when:
        log.info("Starting enj test")
        def result = test(loadBars("data_enj.csv"), modelFile, false, null)
        log.info("Ended enj test")

        then:
        result
    }

    def 'test moa feature selection for iota'() {
        when:
        log.info("Starting iota test")
        def result = test(loadBars("data_iota.csv"), modelFile, false, null)
        log.info("Ended iota test")

        then:
        result
    }

    def 'test moa feature selection for ltc'() {
        when:
        log.info("Starting ltc test")
        def result = test(loadBars("data_ltc.csv"), modelFile, false, null)
        log.info("Ended ltc test")

        then:
        result
    }

    def 'test moa feature selection for qtum'() {
        when:
        log.info("Starting qtum test")
        def result = test(loadBars("data_qtum.csv"), modelFile, false, null)
        log.info("Ended qtum test")

        then:
        result
    }

    def 'test moa online'() {
        when:
        var onlineClassifier = new OnlineClassifier(
            () -> new DatasetSupplier(null, null, false).getIndicatorSet(),
            ValidationMetricSet::new,
            () -> new MoaClassifier("efdt.VFDT -d FastNominalAttributeClassObserver"),
            modelFile,
            Dataset.features("SFA(QuantIndicator(16,4,2,VSCT(12,Close))[75])[2], SFA(QuantIndicator(16,4,2,VSCT(20,Close))[158])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,15,Close))[172])[2], SFA(QuantIndicator(16,4,2,CRSI(10,5,10,Close))[50])[2], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[153])[0], SFA(QuantIndicator(16,4,2,CRSI(25,5,10,Close))[172])[3], SFA(QuantIndicator(16,4,2,ARSI(14,Close))[162])[0], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[75])[0], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[55])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,5,Close))[102])[3], SFA(QuantIndicator(16,4,2,MFI(6))[167])[2], SFA(QuantIndicator(16,4,2,ARSI(2,Close))[128])[3], SFA(QuantIndicator(16,4,2,MFI(28))[146])[0], SFA(QuantIndicator(16,4,2,ARSI(2,Close))[172])[0], SFA(QuantIndicator(16,4,2,ARSI(28,Close))[54])[3], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[0])[3], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[115])[2], SFA(QuantIndicator(16,4,2,MFI(30))[19])[5], SFA(QuantIndicator(16,4,2,VSCT(4,Close))[112])[3], SFA(QuantIndicator(16,4,2,MFI(16))[170])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,10,Close))[7])[0], SFA(QuantIndicator(16,4,2,MFI(14))[110])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,10,Close))[75])[0], SFA(QuantIndicator(16,4,2,ARSI(4,Close))[76])[0], SFA(QuantIndicator(16,4,2,MFI(2))[172])[0], SFA(QuantIndicator(16,4,2,MFI(10))[70])[0], SFA(QuantIndicator(16,4,2,MFI(18))[138])[2], SFA(QuantIndicator(16,4,2,VSCT(6,Close))[102])[3], SFA(QuantIndicator(16,4,2,CRSI(25,5,10,Close))[100])[4], SFA(QuantIndicator(16,4,2,MFI(24))[108])[0], SFA(QuantIndicator(16,4,2,MFI(2))[59])[0], SFA(QuantIndicator(16,4,2,CRSI(10,5,15,Close))[70])[2], SFA(QuantIndicator(16,4,2,CRSI(15,5,15,Close))[123])[5], SFA(QuantIndicator(16,4,2,MFI(6))[19])[3], SFA(QuantIndicator(16,4,2,MFI(4))[103])[5], SFA(QuantIndicator(16,4,2,VSCT(2,Close))[105])[0], SFA(QuantIndicator(16,4,2,MFI(4))[11])[2], SFA(QuantIndicator(16,4,2,VSCT(6,Close))[15])[5], SFA(QuantIndicator(16,4,2,VSCT(20,Close))[162])[0], SFA(QuantIndicator(16,4,2,MFI(32))[70])[2], SFA(QuantIndicator(16,4,2,ARSI(30,Close))[54])[3], SFA(QuantIndicator(16,4,2,ARSI(10,Close))[109])[3], SFA(QuantIndicator(16,4,2,MFI(12))[129])[3], SFA(QuantIndicator(16,4,2,ARSI(18,Close))[60])[0], SFA(QuantIndicator(16,4,2,VSCT(26,Close))[162])[0], SFA(QuantIndicator(16,4,2,ARSI(12,Close))[158])[5]", Dataset.DEFAULT),
            0.0004d,
            1332
        )
        onlineClassifier.init(loadBars("data.csv"))
        println onlineClassifier.getLastTradeMetric()
        println Arrays.toString(onlineClassifier.getMetrics())

        then:
        true
    }

    def 'test different online label producers'() {
        when:
        def bars = loadBars("data.csv").stream().toList()
        Supplier<IndicatorSet<Bar>> indicators = () -> new IndicatorSet<>(
                new Close().rename("price"),
                new Close().rename("trendPrice"),
                Indicator.of((b) -> -1, "class")
        )
        for (double a : [0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1]) {
            def te1 = new TradingEngine(0.0004d, Instance::actual)
            def te2 = new TradingEngine(0.0004d, Instance::actual)
            def counterBuy1 = new AtomicLong(0);
            def counterSell1 = new AtomicLong(0);
            def counterBuy2 = new AtomicLong(0);
            def counterSell2 = new AtomicLong(0);
            def ds1 = indicators.get()
                    .toInstanceStream(bars.stream(), new TrendLabel(a), bars.size())
                    .peek(i -> {
                        if (i.actual() == LabelProducer.Label.BUY) {
                            counterBuy1.incrementAndGet()
                        } else if (i.actual() == LabelProducer.Label.SELL) {
                            counterSell1.incrementAndGet()
                        }
                    })
                    .map(te1)
                    .reduce((c, b) -> b)
                    .orElse(null)
            def ds2 = indicators.get()
                    .toInstanceStream(bars.stream(), new DirectionalChange(a), bars.size())
                    .peek(i -> {
                        if (i.actual() == LabelProducer.Label.BUY) {
                            counterBuy2.incrementAndGet()
                        } else if (i.actual() == LabelProducer.Label.SELL) {
                            counterSell2.incrementAndGet()
                        }
                    })
                    .map(te2)
                    .reduce((c, b) -> b)
                    .orElse(null)
            log.info("a: {}, BUY {}, SELL {}, TrendLabel: {}", a, counterBuy1.get(), counterSell1.get(), ds1)
            log.info("a: {}, BUY {}, SELL {}, DirectionalChange: {}", a, counterBuy2.get(), counterSell2.get(), ds2)

        }

        then:
        true
    }

    public static List<FeatureSelectionModel.Result> test(List<Bar> bars, File modelFile, boolean createModel, String initialFeatures) {
        var tempDatasetFile = File.createTempFile("dataset", ".csv")
        tempDatasetFile.deleteOnExit()
        String[][] initial = new String[1][];
        if (initialFeatures != null) {
            initial[0] = Dataset.features(initialFeatures, null);
        } else {
            initial = new String[1][0];
        }
        var dataset = new DatasetSupplier(() -> bars.stream(), modelFile, createModel).get()
        var temp = Arrays.stream(dataset.data()); // maybe reduce?
        Dataset.save(tempDatasetFile, temp, dataset.features())
        log.info("Dataset saved to {}", tempDatasetFile.absolutePath)
        def mrmreSelected = MRMRe.mRMRe(tempDatasetFile.absolutePath, 2000, 50, "0.005", "0.005", "0", "0")
        log.info("MRMRe selected features: {}", mrmreSelected.length)
        dataset = dataset.select(Stream.of(Arrays.stream(initial[0]), Arrays.stream(mrmreSelected), ["price", "trendPrice", "class"].stream())
                .flatMap(s -> s).distinct().toArray(String[]::new))
        Supplier<ValidationMetricSet> metricSet = () -> new ValidationMetricSet();
       // Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new NNClassifier(2), 0.0004d, metricSet.get());
       // Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new MoaClassifier("efdt.VFDT -d FastNominalAttributeClassObserver"), 0.0004d, metricSet.get());
        Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new MoaClassifier("efdt.VFDT -d FastNominalAttributeClassObserver"), 0.0004d, metricSet.get());
        log.info("Starting feature selection")
        var model = new BaseFeatureSelectionModel(5, Integer.MAX_VALUE, new String[] {"MCC", "CrossEntropy"}, strategy, TrainTestProvider.create(dataset, 1,0.7))
        return model.runAll(dataset.features(), initial, 10)
    }

    public static Dataset fitSfaModel(List<Bar> bars, File modelFile) {
        new DatasetSupplier(() -> bars.stream(), modelFile, true).get()
    }
}
