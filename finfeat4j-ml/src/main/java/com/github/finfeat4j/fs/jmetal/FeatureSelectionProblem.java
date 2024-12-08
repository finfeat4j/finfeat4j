package com.github.finfeat4j.fs.jmetal;

import org.slf4j.Logger;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.catalogue.common.evaluation.impl.MultiThreadedEvaluation;
import org.uma.jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import org.uma.jmetal.component.catalogue.ea.replacement.Replacement;
import org.uma.jmetal.component.catalogue.ea.replacement.impl.RankingAndDensityEstimatorReplacement;
import org.uma.jmetal.component.catalogue.ea.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.component.catalogue.ea.variation.impl.CrossoverAndMutationVariation;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.comparator.MultiComparator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class FeatureSelectionProblem extends AbstractBinaryProblem {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(FeatureSelectionProblem.class);

  private final Function<String[], double[]> fitness;
  private final int totalFeatures;
  private final int metricSize;
  private final List<Integer> bits;
  private final boolean[] optimization;
  private final double[] empty;
  private final String[] columNames;

  public FeatureSelectionProblem(String[] columNames,
                                 boolean[] optimization, int metricsSize,
                                 Function<String[], double[]> fitness) {
    this.fitness = fitness;
    this.totalFeatures = columNames.length;
    this.bits = List.of(totalFeatures);
    this.metricSize = metricsSize;
    this.optimization = optimization;
    this.empty = new double[optimization.length];
    this.columNames = columNames;
    for (int i = 0; i < empty.length; i++) {
      if (optimization[i]) {
        this.empty[i] = Double.NEGATIVE_INFINITY;
      } else {
        this.empty[i] = Double.POSITIVE_INFINITY;
      }
    }
  }

  @Override
  public List<Integer> numberOfBitsPerVariable() {
    return bits;
  }

  @Override
  public int numberOfVariables() {
    return totalFeatures;
  }

  @Override
  public int numberOfObjectives() {
    return metricSize;
  }

  @Override
  public int numberOfConstraints() {
    return 0;
  }

  @Override
  public String name() {
    return "FeatureSelection";
  }

  @Override
  public BinarySolution evaluate(BinarySolution solution) {
    var bits = solution.variables().getFirst();
    var features = bits.stream()
            .mapToObj(i -> columNames[i])
            .toArray(String[]::new);
    var result = fitness.apply(features);
    if (result == null) {
      log.error("Returned null for {}", Arrays.toString(features));
    }
    for (int i = 0; i < metricSize; i++) {
      solution.objectives()[i] = optimization[i] ? -result[i] : result[i];
    }
    solution.attributes().put("objectives", result);
    return solution;
  }

  public static List<BinarySolution> initialSolutions(String[] features, String[][] initial, int metricSize, Problem<BinarySolution> problem) {
    var solutions = new ArrayList<BinarySolution>();
    var bits = List.of(features.length);
    for (var sol : initial) {
      var solution = new DefaultBinarySolution(bits, metricSize);
      var bitSet = solution.variables().get(0);
      bitSet.clear();
      for (var feat : sol) {
        var index = IntStream.range(0, features.length)
                .filter(i -> features[i].equals(feat))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
          bitSet.set(index, true);
        } else {
          log.debug("Feature {} not found in {}", feat, Arrays.toString(features));
        }
      }
      solutions.add(solution);
    }
    while (solutions.size() < 300) {
      solutions.add(problem.createSolution());
    }
    return solutions;
  }

  public static <S extends BinarySolution> EvolutionaryAlgorithm<S> algorithm(
          Problem<S> problem, String[] features, boolean[] maximize, int metricSize, SolutionsCreation<S> initial,
          List<BinarySolution> bestOnes) {
    var evaluator = new MultiThreadedEvaluation<>(0, problem);
    double crossoverProbability = 0.9;
    var crossover = new HalfUniformCrossover<S>(crossoverProbability);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    var mutation = new BitFlipMutation<S>(mutationProbability);
    var densityEstimator = new CrowdingDistanceDensityEstimator<S>();
    var ranking = new FastNonDominatedSortRanking<S>();

    var replacement =
            new RankingAndDensityEstimatorReplacement<>(
                    ranking, densityEstimator, Replacement.RemovalPolicy.ONE_SHOT);

    var variation =
            new CrossoverAndMutationVariation<>(
                    100, crossover, mutation);

    int tournamentSize = 2;
    var selection =
            new NaryTournamentSelection<S>(
                    tournamentSize,
                    variation.getMatingPoolSize(),
                    new MultiComparator<S>(
                            Arrays.asList(
                                    Comparator.comparing(ranking::getRank),
                                    Comparator.comparing(densityEstimator::value).reversed())));
    var steadyTermination = new TerminationBySteadyFitness(features, maximize, metricSize, bestOnes);

    return new EvolutionaryAlgorithm<S>("NSGAII", initial, evaluator, steadyTermination,
            selection, variation, replacement);
  }


}