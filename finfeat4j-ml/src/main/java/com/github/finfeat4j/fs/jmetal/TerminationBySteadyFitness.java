package com.github.finfeat4j.fs.jmetal;

import org.slf4j.Logger;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.errorchecking.Check;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Termination criterion based on the steady fitness of the population.
 * If the fitness of the population does not change for a given number of iterations, the algorithm stops.
 * It also collects best found solutions into bestOnes list.
 */
public class TerminationBySteadyFitness implements Termination {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(TerminationBySteadyFitness.class);

  private final AtomicLong steadyFitnessCounter = new AtomicLong(200);

  private String[] features;

  private int metricSize;

  private double[] bestObjectives;

  private boolean[] maximize;

  private List<BinarySolution> bestOnes;

  private BinarySolution[][] bestSolutionsPerMetric;

  private Set<Integer> validatedSolutions = new HashSet<>();

  public TerminationBySteadyFitness(String[] features, boolean[] maximize, int metricSize, List<BinarySolution> bestOnes) {
    this.features = features;
    this.bestOnes = bestOnes;
    this.metricSize = metricSize;
    this.bestObjectives = new double[metricSize];
    this.bestSolutionsPerMetric = new BinarySolution[metricSize][metricSize];
    for (int i = 0; i < metricSize; i++) {
      if (maximize[i]) {
        this.bestObjectives[i] = Double.NEGATIVE_INFINITY;
      } else {
        this.bestObjectives[i] = Double.POSITIVE_INFINITY;
      }
    }
    this.maximize = maximize;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isMet(Map<String, Object> algorithmStatusData) {
    Check.notNull(algorithmStatusData.get("EVALUATIONS"));
    var population = (List<BinarySolution>) algorithmStatusData.get("POPULATION");
    var counter = this.steadyFitnessCounter.decrementAndGet();
    if (counter <= 0) {
      bestOnes.clear();
      bestOnes.addAll(Arrays.stream(bestSolutionsPerMetric)
              .flatMap(Stream::of)
              .filter(Objects::nonNull)
              .distinct()
              .toList());
      return true;
    }
    for (var solution : population) {
      var hash = solution.variables().getFirst().hashCode();
      if (validatedSolutions.contains(hash)) {
        continue;
      }
      validatedSolutions.add(hash);

      var objectives = getObjectives(solution);
      var foundAnyBetter = false;
      for (int i = 0; i < metricSize; i++) {
        var foundBetter = isBetter(objectives[i], bestObjectives[i], maximize[i]) ||
                (objectives[i] == bestObjectives[i] && bestSolutionsPerMetric[i][i] != null && isBetterSolution(solution, bestSolutionsPerMetric[i][i]));
        if (foundBetter) {
          bestObjectives[i] = objectives[i];
          Arrays.fill(bestSolutionsPerMetric[i], solution);
          foundAnyBetter = true;
        } else if (objectives[i] == bestObjectives[i] && bestSolutionsPerMetric[i][i] != null) {
          for (int j = 0; j < metricSize; j++) {
            if (i == j) {
              continue;
            }
            var bestObjectives = getObjectives(bestSolutionsPerMetric[i][j]);
            if (isBetter(objectives[j], bestObjectives[j], maximize[j])) {
              bestSolutionsPerMetric[i][j] = solution;
              foundAnyBetter = true;
            }
          }
        }
      }
      if (foundAnyBetter) {
        var features = getFeatures(this.features, solution);
        log.debug("{} ({}) : {}", Arrays.toString(objectives), features.size(), String.join(", ", features));
        // add some iterations
        steadyFitnessCounter.set(Math.min(200, counter + 100));
      }
    }
    return false;
  }

  private boolean isBetter(double one, double another, boolean maximize) {
    return (one > another && maximize) || (one < another && !maximize);
  }

  private boolean isBetterSolution(BinarySolution newSolution, BinarySolution currentBest) {
    return currentBest.variables().getFirst().cardinality() > newSolution.variables().getFirst().cardinality();
  }

  private double[] getObjectives(BinarySolution solution) {
    return (double[]) solution.attributes().get("objectives");
  }

  public static Collection<String> getFeatures(String[] features, BinarySolution solution) {
    var set = new LinkedHashSet<String>(features.length);
    solution.variables().get(0).stream()
      .mapToObj(i -> features[i])
      .forEach(set::add);
    return set;
  }
}