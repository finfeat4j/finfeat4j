package com.github.finfeat4j.fs.jmetal;

import org.slf4j.Logger;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.errorchecking.Check;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class TerminationBySteadyFitness implements Termination {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(TerminationBySteadyFitness.class);

  private final AtomicLong steadyFitnessCounter = new AtomicLong(0);

  private String[] features;

  private int metricSize;

  private double[] bestObjectives;

  private boolean[] maximize;

  private List<BinarySolution> bestOnes;

  public TerminationBySteadyFitness(String[] features, boolean[] maximize, int metricSize, List<BinarySolution> bestOnes) {
    this.features = features;
    this.bestOnes = bestOnes;
    this.metricSize = metricSize;
    this.bestObjectives = new double[metricSize];
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
    this.steadyFitnessCounter.incrementAndGet();
    if (this.steadyFitnessCounter.get() >= 150) {
      return true;
    }
    for (var solution : population) {
      var features = getFeatures(this.features, solution);
      var objs = (double[]) solution.attributes().get("objectives");
      boolean found = false;
      for (int i = 0; i < metricSize; i++) {
        if ((objs[i] > bestObjectives[i] && maximize[i]) || (objs[i] < bestObjectives[i] && !maximize[i])) {
          bestObjectives[i] = objs[i];
          steadyFitnessCounter.set(0);
          if (!found) {
            found = true;
            bestOnes.add(solution);
              log.debug("{} ({}) : {}", Arrays.toString(objs), features.length, Arrays.toString(features));
          }
        }
      }
    }
    return false;
  }

  public List<BinarySolution> getBestOnes() {
    return bestOnes;
  }

  public static String[] getFeatures(String[] features, BinarySolution solution) {
    return solution.variables().get(0).stream()
            .mapToObj(i -> features[i])
            .toArray(String[]::new);
  }
}