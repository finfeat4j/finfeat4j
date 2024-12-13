package com.github.finfeat4j.fs.smile

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.fs.DatasetSupplier
import com.github.finfeat4j.fs.jmetal.FeatureSelectionModel
import com.github.finfeat4j.trading.TradingEngine
import com.github.finfeat4j.util.ToIntArrayConverter
import smile.classification.Classifier
import smile.classification.DiscreteNaiveBayes

import java.util.function.BiFunction
import java.util.function.Function

class SmileFeatureSelectionModelSpec extends BaseSpec {

    def 'test smile NB feature selection'() {
        given:
        def dataset = new DatasetSupplier(() -> BARS.stream()).get()
        def mapper = new ToIntArrayConverter()

        Function<FeatureSelectionModel.SplitSet, Classifier<int[]>> train = (FeatureSelectionModel.SplitSet trainSplit) -> {
            var trainX = trainSplit.x().map(mapper).toArray(int[][]::new);
            var trainY = trainSplit.y().toArray();
            var nb = new DiscreteNaiveBayes(DiscreteNaiveBayes.Model.TWCNB, 2, trainX[0].length);
            nb.update(trainX, trainY);
            return nb;
        }


        BiFunction<FeatureSelectionModel.SplitSet, Classifier<int[]>, double[]> test = (FeatureSelectionModel.SplitSet testSplit, Classifier<int[]> classifier) -> {
            var testX = testSplit.x().map(mapper).toArray(int[][]::new);
            return classifier.predict(testX);
        }

        when:
        def model = new SmileFeatureSelectionModel(train, test, dataset, 0.8d, () -> new TradingEngine(0.0004))
        def result = model.runAll(dataset.features(), new String[0][], 10)

        then:
        result
    }
}
