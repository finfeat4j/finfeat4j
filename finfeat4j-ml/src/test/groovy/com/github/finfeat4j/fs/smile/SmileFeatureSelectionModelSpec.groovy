package com.github.finfeat4j.fs.smile

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.fs.DatasetSupplier
import com.github.finfeat4j.fs.MetricAwareClassifier
import com.github.finfeat4j.fs.TrainTestProvider
import com.github.finfeat4j.fs.jmetal.BaseFeatureSelectionModel
import com.github.finfeat4j.fs.jmetal.FeatureSelectionModel
import com.github.finfeat4j.fs.mrmre.MRMRe
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.DirectionalChange
import com.github.finfeat4j.label.Instance
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.validation.ValidationMetricSet
import com.github.finfeat4j.strategy.SmileClassifier
import com.github.finfeat4j.validation.TradingEngine
import com.github.finfeat4j.core.Dataset
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Supplier
import java.util.stream.Stream

class SmileFeatureSelectionModelSpec extends BaseSpec {

    private static final Logger log = LoggerFactory.getLogger(MoaFeatureSelectionModelSpec.class)
    private final File modelFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/rmodel.ser");
    private final File datasetFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/dataset.csv");

    def 'test fit sfa model'() {
        when:
        fitSfaModel(loadBars("data.csv"), modelFile)

        then:
        true
    }

    def 'test smile feature selection' () {
        when:
        log.info("Starting btc test")
        def result = test(loadBars("data.csv"), modelFile, false, null)
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
            def ds1 = indicators.get()
                    .toInstanceStream(bars.stream(), new TrendLabel(a), bars.size())
                    .map(te1)
                    .reduce((c, b) -> b)
                    .orElse(null)
            def ds2 = indicators.get()
                    .toInstanceStream(bars.stream(), new DirectionalChange(a), bars.size())
                    .map(te2)
                    .reduce((c, b) -> b)
                    .orElse(null)
            log.info("a: {}, TrendLabel: {}", a, ds1)
            log.info("a: {}, DirectionalChange: {}", a, ds2)

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
        var temp = Arrays.stream(dataset.data()).toArray(double[][]::new);
        var tempDataset = new Dataset(dataset.features(), temp);
        tempDataset.save(tempDatasetFile)
        log.info("Dataset saved to {}", tempDatasetFile.absolutePath)
        def mrmreSelected = MRMRe.mRMRe(tempDatasetFile.absolutePath, 2000, 50, "0", "0", "0", "0")
        log.info("MRMRe selected features: {}", mrmreSelected.length)
        dataset = dataset.select(Stream.of(Arrays.stream(initial[0]), Arrays.stream(mrmreSelected), ["price", "trendPrice", "class"].stream())
                .flatMap(s -> s).distinct().toArray(String[]::new))
        Supplier<ValidationMetricSet> metricSet = () -> new ValidationMetricSet();
        Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new SmileClassifier(SmileClassifier.NB_TRAIN, SmileClassifier.TEST), 0.0004d, metricSet.get());
        log.info("Starting feature selection")
        var model = new BaseFeatureSelectionModel(3, Integer.MAX_VALUE, new String[] {"MCC", "CrossEntropy"}, strategy, TrainTestProvider.create(dataset, 2, 0.8))
        var result = model.runAll(dataset.features(), initial, 5)
        return result;
    }

    public static List<FeatureSelectionModel.Result> test(File datasetFile, String initialFeatures) {
        String[][] initial = new String[1][];
        if (initialFeatures != null) {
            initial[0] = Dataset.features(initialFeatures, null);
        } else {
            initial = new String[1][0];
        }
        def mrmreSelected = MRMRe.mRMRe(datasetFile.absolutePath, 2000, 50, "0", "0", "0", "0")
        log.info("MRMRe selected features: {}", mrmreSelected.length)
        var dataset = Dataset.load(datasetFile).select(Stream.of(Arrays.stream(initial[0]), Arrays.stream(mrmreSelected), ["price", "trendPrice", "class"].stream())
                .flatMap(s -> s).distinct().toArray(String[]::new))
        Supplier<ValidationMetricSet> metricSet = () -> new ValidationMetricSet();
        Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new SmileClassifier(SmileClassifier.NB_TRAIN, SmileClassifier.TEST), 0.0004d, metricSet.get());
        log.info("Starting feature selection")
        var model = new BaseFeatureSelectionModel(3, Integer.MAX_VALUE, new String[] {"MCC", "CrossEntropy"}, strategy, TrainTestProvider.create(dataset, 1, 0.8))
        var result = model.runAll(dataset.features(), initial, 5)
        return result;
    }

    public static Dataset fitSfaModel(List<Bar> bars, File modelFile) {
        new DatasetSupplier(() -> bars.stream(), modelFile, true).get()
    }
}
