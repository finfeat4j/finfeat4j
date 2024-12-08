package com.github.finfeat4j.fs.jmetal;

import com.github.finfeat4j.fs.Metric;
import org.slf4j.Logger;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.finfeat4j.fs.jmetal.FeatureSelectionProblem.algorithm;
import static com.github.finfeat4j.fs.jmetal.FeatureSelectionProblem.initialSolutions;
import static com.github.finfeat4j.fs.jmetal.TerminationBySteadyFitness.getFeatures;

public interface FeatureSelectionModel<T> {

    Logger log = org.slf4j.LoggerFactory.getLogger(FeatureSelectionModel.class);

    double[] fitness(String[] features);

    boolean[] maximize();

    List<Metric<T>> metrics();

    List<Metric<T>> allMetrics();

    default int metricSize() {
        return metrics().size();
    }

    double[] worstObjectives();

    Set<String> skipFeatures();

    default List<Result> runOnce(String[] features, String[][] initial) {
        var problem = new FeatureSelectionProblem(features, this.maximize(), this.metricSize(), this::fitness);
        var solutions = new ArrayList<BinarySolution>();
        var algo = algorithm(problem, features, this.maximize(), this.metricSize(), () -> initialSolutions(features, initial, this.metricSize(), problem), solutions);
        new AlgorithmRunner.Executor(algo).execute();
        var bestOnes = new ArrayList<BinarySolution>();
        for (int i = 0; i < this.metricSize(); i++) {
            var best = solutions.stream()
                    .min(new ObjectiveComparator<>(i))
                    .orElse(null);
            if (best != null) {
                bestOnes.add(best);
                var featuresSelected = getFeatures(features, best);
                log.debug("Best {}: {} ({}): {}", i, Arrays.toString(best.objectives()), featuresSelected.length, Arrays.toString(featuresSelected));
            }
        }
        var results = new ArrayList<Result>();
        for (var res : bestOnes) {
            var obj = (double[]) res
                    .attributes()
                    .get("objectives");
            var featuresSelected = getFeatures(features, res);
            results.add(new Result(featuresSelected, obj));
            log.debug("{} ({}): {}", Arrays.toString(obj), featuresSelected.length, Arrays.toString(featuresSelected));
        }
        return results;
    }

    default List<Result> runAll(String[] colNames, String[][] initial, int maxFeatures) {
        int skip = 0;
        var skipFeatures = skipFeatures();
        String[] featureSet = Stream.of(colNames)
                .filter(c -> !skipFeatures.contains(c))
                .limit(maxFeatures)
                .toArray(String[]::new);
        List<Result> results = new ArrayList<>();
        var init = initial;
        while (featureSet.length > 0) {
            var combined = Stream.of(Stream.of(featureSet), Stream.of(init).flatMap(Arrays::stream)).flatMap(Function.identity()).distinct().toArray(String[]::new);
            log.debug("Running with [{}] {} features", combined.length, Arrays.toString(combined));
            results = runOnce(combined, init);
            init = results.stream().map(Result::features).toArray(String[][]::new);
            log.debug("Done");
            skip += maxFeatures;
            featureSet = Stream.of(colNames)
                    .filter(c -> !skipFeatures.contains(c))
                    .skip(skip)
                    .limit(maxFeatures)
                    .distinct()
                    .toArray(String[]::new);
        }
        return results;
    }

    record Result(String[] features, double[] objectives) {
    }

    record SplitSet(Stream<double[]> x, IntStream y) {

    }
}
