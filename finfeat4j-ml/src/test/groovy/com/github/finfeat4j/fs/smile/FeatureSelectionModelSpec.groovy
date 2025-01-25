package com.github.finfeat4j.fs.smile

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Dataset
import com.github.finfeat4j.builders.DatasetBuilder
import com.github.finfeat4j.builders.ExperimentBuilder
import com.github.finfeat4j.classifier.MoaClassifier
import com.github.finfeat4j.core.DoubleDataset
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.fs.DatasetSupplier
import com.github.finfeat4j.fs.OnlineClassifier
import com.github.finfeat4j.label.TrendLabel
import com.github.finfeat4j.validation.ValidationMetricSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Supplier

class FeatureSelectionModelSpec extends BaseSpec {

    private static final Logger log = LoggerFactory.getLogger(FeatureSelectionModelSpec.class)
    private final File modelFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/rmodel.ser");
    private final File datasetFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/dataset.csv");
    private final File featureFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/features.csv");
    private final boolean useSFA = true;

    def 'test experiment'() {
        when:
        def bars = loadBars("data.csv");
        Supplier<IndicatorSet<Bar>> indicatorSet = new DatasetSupplier()
        def dataset = new DatasetBuilder()
            .datasetFile(datasetFile)
            .sfaModelFile(modelFile)
            .featureFile(featureFile)
            .useSFA(useSFA)
            .indicators(indicatorSet)
            .createSFAModel(true)
            .labelProducer(() -> new TrendLabel(0.1))
            .stream(() -> bars.stream())

        new ExperimentBuilder()
            .datasetBuilder(dataset)
            .featuresFile(featureFile)
            .classifier(() -> new MoaClassifier("efdt.VFDT -c 0.001", useSFA))
            .validationMetricSet(() -> new ValidationMetricSet())
            .run()

        then:
        true
    }

    def 'test existing dataset experiment'() {
        when:
        new ExperimentBuilder()
                .datasetFile(datasetFile)
                .featuresFile(featureFile)
                .classifier(() -> new MoaClassifier("efdt.VFDT", useSFA))
                .validationMetricSet(() -> new ValidationMetricSet())
                .run()

        then:
        true
    }

    def 'test existing sfa model'() {
        when:
        def tempDs = File.createTempFile("dataset", ".csv")
        tempDs.deleteOnExit()
        def tempEthFeatureFile = File.createTempFile("features", ".csv")
        tempEthFeatureFile.deleteOnExit()

        def bars = loadBars("data_eth.csv");
        Supplier<IndicatorSet<Bar>> indicatorSet = new DatasetSupplier()
        def dataset = new DatasetBuilder()
                .datasetFile(tempDs)
                .sfaModelFile(modelFile)
                .featureFile(tempEthFeatureFile)
                .useSFA(useSFA)
                .indicators(indicatorSet)
                .createSFAModel(false)
                .labelProducer(() -> new TrendLabel(0.1))
                .stream(() -> bars.stream())

        new ExperimentBuilder()
                .datasetBuilder(dataset)
                .featuresFile(tempEthFeatureFile)
                .classifier(() -> new MoaClassifier("efdt.VFDT -c 0.001", useSFA))
                .validationMetricSet(() -> new ValidationMetricSet())
                .run()

        then:
        true
    }

    def 'test online'() {
        when:
        var onlineClassifier = new OnlineClassifier(
                new DatasetSupplier(),
                ValidationMetricSet::new,
                () -> new MoaClassifier("efdt.VFDT -c 0.001", useSFA),
                () -> new TrendLabel(0.1),
                useSFA,
                modelFile,
                Dataset.features("SFA(QuantIndicator(16,4,2,NET(7,MFI(32)))[104])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(2)))[146])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(26)))[146])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(32)))[117])[2], SFA(QuantIndicator(16,4,2,NET(3,MFI(16)))[173])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(24,Close)))[153])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(20)))[117])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(24)))[65])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(24)))[151])[2], SFA(QuantIndicator(16,4,2,NET(7,MFI(6)))[41])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(32)))[125])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(8)))[154])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(26)))[169])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(30)))[57])[3], SFA(QuantIndicator(16,4,2,NET(11,ARSI(2,Close)))[159])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(10)))[117])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(24)))[49])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(28)))[162])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(32)))[162])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(14)))[156])[3], SFA(QuantIndicator(16,4,2,NET(3,ARSI(6,Close)))[156])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(8)))[117])[2], SFA(QuantIndicator(16,4,2,NET(7,MFI(30)))[104])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(12)))[152])[2], SFA(QuantIndicator(16,4,2,NET(11,ARSI(2,Close)))[169])[3], SFA(QuantIndicator(16,4,2,NET(3,ARSI(4,Close)))[156])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(30)))[109])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(6)))[152])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(30)))[166])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(8)))[14])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(6)))[151])[2], SFA(QuantIndicator(16,4,2,NET(3,MFI(16)))[49])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(2,Close)))[148])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(26)))[125])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(24)))[125])[0], SFA(QuantIndicator(16,4,2,NET(3,ARSI(16,Close)))[100])[2], SFA(QuantIndicator(16,4,2,NET(3,MFI(24)))[152])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(22)))[156])[3], SFA(QuantIndicator(16,4,2,NET(7,MFI(10)))[111])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(2)))[109])[0], SFA(QuantIndicator(16,4,2,NET(3,ARSI(2,Close)))[156])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(4)))[148])[2], SFA(QuantIndicator(16,4,2,NET(7,MFI(22)))[154])[2], SFA(QuantIndicator(16,4,2,NET(7,WLR(24)))[170])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(4)))[150])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(20)))[170])[0], SFA(QuantIndicator(16,4,2,NET(3,ARSI(4,Close)))[111])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(16)))[98])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(26)))[173])[2], SFA(QuantIndicator(16,4,2,NET(3,MFI(20)))[138])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(10)))[119])[2], SFA(QuantIndicator(16,4,2,NET(7,WLR(2)))[155])[3], SFA(QuantIndicator(16,4,2,NET(3,ARSI(26,Close)))[159])[3], SFA(QuantIndicator(16,4,2,NET(7,WLR(10)))[158])[2], SFA(QuantIndicator(16,4,2,NET(11,ARSI(2,Close)))[151])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(22)))[111])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(6,Close)))[168])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(4)))[152])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(6)))[158])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(2)))[18])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(14)))[123])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(2)))[156])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(14)))[18])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(10)))[111])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(4)))[150])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(10)))[155])[0], SFA(QuantIndicator(16,4,2,NET(11,MFI(2)))[115])[2], SFA(QuantIndicator(16,4,2,NET(11,ARSI(12,Close)))[102])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(22)))[117])[0], SFA(QuantIndicator(16,4,2,NET(3,WLR(2)))[173])[2], SFA(QuantIndicator(16,4,2,NET(7,ARSI(28,Close)))[4])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(2)))[111])[3], SFA(QuantIndicator(16,4,2,NET(11,WLR(26)))[6])[3], SFA(QuantIndicator(16,4,2,NET(11,WLR(30)))[173])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(28)))[147])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(6)))[162])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(32)))[154])[3], SFA(QuantIndicator(16,4,2,NET(3,WLR(12)))[104])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(24)))[65])[3], SFA(QuantIndicator(16,4,2,NET(7,ARSI(6,Close)))[104])[3], SFA(QuantIndicator(16,4,2,NET(3,ARSI(22,Close)))[115])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(6)))[123])[2], SFA(QuantIndicator(16,4,2,NET(7,WLR(8)))[37])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(20)))[155])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(22)))[173])[0], SFA(QuantIndicator(16,4,2,NET(7,ARSI(30,Close)))[170])[0], SFA(QuantIndicator(16,4,2,NET(11,ARSI(12,Close)))[45])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(2)))[172])[3], SFA(QuantIndicator(16,4,2,NET(3,MFI(10)))[159])[3], SFA(QuantIndicator(16,4,2,NET(11,MFI(6)))[102])[3], SFA(QuantIndicator(16,4,2,NET(11,WLR(10)))[166])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(24,Close)))[104])[2], SFA(QuantIndicator(16,4,2,NET(11,MFI(16)))[166])[2], SFA(QuantIndicator(16,4,2,NET(7,WLR(28)))[127])[0], SFA(QuantIndicator(16,4,2,NET(3,MFI(12)))[169])[0], SFA(QuantIndicator(16,4,2,NET(7,ARSI(4,Close)))[109])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(18)))[146])[3], SFA(QuantIndicator(16,4,2,NET(7,ARSI(10,Close)))[105])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(26)))[169])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(10)))[152])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(2,Close)))[146])[2], SFA(QuantIndicator(16,4,2,NET(7,WLR(28)))[147])[3], SFA(QuantIndicator(16,4,2,NET(7,MFI(6)))[138])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(30)))[55])[3], SFA(QuantIndicator(16,4,2,NET(7,ARSI(4,Close)))[172])[2], SFA(QuantIndicator(16,4,2,NET(3,ARSI(8,Close)))[127])[2], SFA(QuantIndicator(16,4,2,NET(3,WLR(32)))[123])[2], SFA(QuantIndicator(16,4,2,NET(7,MFI(32)))[49])[3], SFA(QuantIndicator(16,4,2,NET(7,MFI(8)))[100])[0], SFA(QuantIndicator(16,4,2,NET(11,WLR(14)))[98])[0], SFA(QuantIndicator(16,4,2,NET(7,MFI(20)))[167])[3], SFA(QuantIndicator(16,4,2,NET(11,MFI(10)))[53])[0], SFA(QuantIndicator(16,4,2,NET(7,WLR(6)))[170])[0]", DoubleDataset.DEFAULT),
                0.0004d, 1522
        )
        onlineClassifier.init(loadBars("data_btc.csv"))
        println onlineClassifier.getLastTradeMetric()
        println Arrays.toString(onlineClassifier.getMetrics())

        then:
        true
    }
}
