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
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


public class FeatureSelectionProblem extends AbstractBinaryProblem {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(FeatureSelectionProblem.class);

  private final Function<String[], double[]> fitness;
  private final int totalFeatures;
  private final int metricSize;
  private final List<Integer> bits;
  private final boolean[] optimization;
  private final String[] columNames;

  public FeatureSelectionProblem(String[] columNames,
                                 boolean[] optimization, int metricsSize,
                                 Function<String[], double[]> fitness) {
    this.fitness = fitness;
    this.totalFeatures = columNames.length;
    this.bits = List.of(totalFeatures);
    this.metricSize = metricsSize;
    this.optimization = optimization;
    this.columNames = columNames;
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
    var features = getFeatures(solution);
    var result = fitness.apply(features);
    if (result == null) {
      throw new RuntimeException("Returned null for " + Arrays.toString(features));
    }
    for (int i = 0; i < metricSize; i++) {
      solution.objectives()[i] = optimization[i] ? -result[i] : result[i];
    }
    solution.attributes().put("objectives", result);
    return solution;
  }

  private String[] getFeatures(BinarySolution solution) {
    return solution.variables().getFirst().stream()
            .mapToObj(i -> columNames[i])
            .toArray(String[]::new);
  }

  public static List<BinarySolution> initialSolutions(String[] features, String[][] initial, int metricSize, Problem<BinarySolution> problem) {
    var solutions = new ArrayList<BinarySolution>();
    var bits = List.of(features.length);
    var indexed = Arrays.asList(features);
    for (var sol : initial) {
      var solution = new DefaultBinarySolution(bits, metricSize);
      var bitSet = solution.variables().getFirst();
      bitSet.clear();
      for (var feat : sol) {
        var index = indexed.indexOf(feat);
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

    var selection = new NaryTournamentSelection<S>(
      new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()), variation.getMatingPoolSize()
    );

    var steadyTermination = new TerminationBySteadyFitness(features, maximize, metricSize, bestOnes);

    return new EvolutionaryAlgorithm<>("NSGAII", initial, evaluator, steadyTermination,
            selection, variation, replacement);
  }


}