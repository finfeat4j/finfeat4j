package com.github.finfeat4j.fs.jmetal;

import org.slf4j.Logger;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.solution.binarysolution.BinarySolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.finfeat4j.fs.jmetal.FeatureSelectionProblem.algorithm;
import static com.github.finfeat4j.fs.jmetal.FeatureSelectionProblem.initialSolutions;
import static com.github.finfeat4j.fs.jmetal.TerminationBySteadyFitness.getFeatures;

public interface FeatureSelectionModel {

    Logger log = org.slf4j.LoggerFactory.getLogger(FeatureSelectionModel.class);

    double[] fitness(String[] features);

    boolean[] maximize();

    int metricSize();

    Set<String> skipFeatures();

    String[] metricNames();

    default List<Result> runOnce(String[] features, String[][] initial) {
        var problem = new FeatureSelectionProblem(features, this.maximize(), this.metricSize(), this::fitness);
        var solutions = new ArrayList<BinarySolution>();
        var algo = algorithm(problem, features, this.maximize(), this.metricSize(),
                () -> initialSolutions(features, initial, this.metricSize(), problem), solutions);
        new AlgorithmRunner.Executor(algo).execute();
        var results = new ArrayList<Result>();
        for (var res : solutions) {
            var obj = (double[]) res.attributes().get("objectives");
            var featuresSelected = getFeatures(features, res);
            results.add(new Result(featuresSelected.toArray(String[]::new), obj));
            log.debug("{} ({}): {}", Arrays.toString(obj), featuresSelected.size(), String.join(", ", featuresSelected));
        }
        return results;
    }

    default List<Result> runAll(String[] colNames, String[][] initial, int maxFeatures) {
        int skip = 0;
        var skipFeatures = skipFeatures();
        List<Result> results = new ArrayList<>();
        var init = initial;

        while (true) {
            String[] featureSet = Stream.of(colNames)
                    .filter(c -> !skipFeatures.contains(c))
                    .skip(skip)
                    .limit(maxFeatures)
                    .toArray(String[]::new);

            if (featureSet.length == 0) break;

            var combined = Stream.concat(Stream.of(featureSet), Stream.of(init).flatMap(Arrays::stream))
                    .distinct()
                    .toArray(String[]::new);

            log.debug("Running with [{}] {} features", combined.length, Arrays.toString(combined));
            log.debug("Metrics: [{}] {}", metricSize(), Arrays.toString(metricNames()));

            results = runOnce(combined, init);
            init = results.stream().map(Result::features).toArray(String[][]::new);

            log.debug("Done");
            skip += maxFeatures;
        }

        return results;
    }

    record Result(String[] features, double[] objectives) {
    }
}
