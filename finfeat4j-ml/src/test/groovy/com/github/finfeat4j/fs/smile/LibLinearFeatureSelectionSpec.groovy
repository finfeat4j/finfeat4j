package com.github.finfeat4j.fs.smile

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator
import com.github.finfeat4j.api.LabelProducer
import com.github.finfeat4j.core.Dataset
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.fs.MetricAwareClassifier
import com.github.finfeat4j.fs.TrainTestProvider
import com.github.finfeat4j.fs.jmetal.BaseFeatureSelectionModel
import com.github.finfeat4j.fs.jmetal.FeatureSelectionModel
import com.github.finfeat4j.fs.DatasetSupplier
import com.github.finfeat4j.fs.mrmre.MRMRe
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.label.DirectionalChange
import com.github.finfeat4j.label.Instance
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.strategy.LibLinearClassifier
import com.github.finfeat4j.strategy.MoaClassifier
import com.github.finfeat4j.validation.TradingEngine
import com.github.finfeat4j.validation.ValidationMetricSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier
import java.util.stream.Stream

class LibLinearFeatureSelectionSpec extends BaseSpec {
    private static final Logger log = LoggerFactory.getLogger(LibLinearFeatureSelectionSpec.class)
    private final File modelFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/rmodel.ser");

    def 'test fit sfa model'() {
        when:
        fitSfaModel(loadBars("data.csv"), modelFile)

        then:
        true
    }

    def 'test liblinear feature selection' () {
        when:
        log.info("Starting btc test")
        def result = test(loadBars("data.csv"), modelFile, false, "SFA(QuantIndicator(16,6,4,CRSI(25,5,5,Close))[128])[5], SFA(QuantIndicator(16,6,4,MFI(4))[81])[0], SFA(QuantIndicator(16,6,4,ARSI(10,Close))[131])[3], SFA(QuantIndicator(16,6,4,ARSI(8,Close))[57])[7], SFA(QuantIndicator(16,6,4,WLR(8))[94])[0], SFA(QuantIndicator(16,6,4,MFI(30))[8])[2], SFA(QuantIndicator(16,6,4,CRSI(20,5,5,Close))[123])[0], SFA(QuantIndicator(16,6,4,CRSI(25,5,10,Close))[83])[0], SFA(QuantIndicator(16,6,4,CRSI(25,5,15,Close))[50])[0], SFA(QuantIndicator(16,6,4,WLR(10))[82])[0], SFA(QuantIndicator(16,6,4,CRSI(10,5,10,Close))[56])[0], SFA(QuantIndicator(16,6,4,ARSI(26,Close))[81])[0], SFA(QuantIndicator(16,6,4,CRSI(25,5,15,Close))[132])[2], SFA(QuantIndicator(16,6,4,WLR(2))[47])[0], SFA(QuantIndicator(16,6,4,ARSI(10,Close))[53])[0], SFA(QuantIndicator(16,6,4,ARSI(2,Close))[132])[0], SFA(QuantIndicator(16,6,4,CRSI(25,5,5,Close))[116])[0], SFA(QuantIndicator(16,6,4,MFI(26))[93])[0], SFA(QuantIndicator(16,6,4,MFI(4))[131])[3], SFA(QuantIndicator(16,6,4,ARSI(4,Close))[131])[0], SFA(QuantIndicator(16,6,4,MFI(4))[86])[3], SFA(QuantIndicator(16,6,4,MFI(4))[3])[2], SFA(QuantIndicator(16,6,4,WLR(26))[112])[3], SFA(QuantIndicator(16,6,4,MFI(20))[129])[0], SFA(QuantIndicator(16,6,4,MFI(14))[95])[2]")
        log.info("Ended btc test")

        then:
        result
    }

    def 'test liblinear feature selection for eth'() {
        when:
        log.info("Starting eth test")
        def result = test(loadBars("data_eth.csv"), modelFile, false, null)
        log.info("Ended eth test")

        then:
        result

    }

    def 'test liblinear feature selection for xrp'() {
        when:
        log.info("Starting xrp test")
        def result = test(loadBars("data_xrp.csv"), modelFile, false, null)
        log.info("Ended xrp test")

        then:
        result

    }

    def 'test liblinear feature selection for bnb'() {
        when:
        log.info("Starting bnb test")
        def result = test(loadBars("data_bnb.csv"), modelFile, false, null)
        log.info("Ended bnb test")

        then:
        result
    }

    def 'test liblinear online'() {
        when:
        def a = true

        then:
        a
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
        def mrmreSelected = MRMRe.mRMRe(tempDatasetFile.absolutePath, 2000, 50, "0", "0", "0", "0")
        log.info("MRMRe selected features: {}", mrmreSelected.length)
        dataset = dataset.select(Stream.of(Arrays.stream(initial[0]), Arrays.stream(mrmreSelected), ["price", "trendPrice", "class"].stream())
                .flatMap(s -> s).distinct().toArray(String[]::new))
        Supplier<ValidationMetricSet> metricSet = () -> new ValidationMetricSet();
        Supplier<MetricAwareClassifier> strategy = () -> new MetricAwareClassifier(new LibLinearClassifier(), 0.0004d, metricSet.get());
        log.info("Starting feature selection")
        var model = new BaseFeatureSelectionModel(5, Integer.MAX_VALUE, new String[] {"MCC", "Error", "F1Score"}, strategy, TrainTestProvider.create(dataset, 3,0.8))
        return model.runAll(dataset.features(), initial, 100)
    }

    public static Dataset fitSfaModel(List<Bar> bars, File modelFile) {
        new DatasetSupplier(() -> bars.stream(), modelFile, true).get()
    }
}
