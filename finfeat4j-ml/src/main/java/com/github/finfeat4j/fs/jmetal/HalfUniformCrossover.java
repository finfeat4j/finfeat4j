package com.github.finfeat4j.fs.jmetal;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.finfeat4j.util.Shuffle.shuffle;

public class HalfUniformCrossover<S extends BinarySolution> implements CrossoverOperator<S> {

    private double crossoverProbability;
    private final RandomGenerator<Double> crossoverRandomGenerator;

    public HalfUniformCrossover(double crossoverProbability) {
        this(crossoverProbability, () -> JMetalRandom.getInstance().nextDouble());
    }

    public HalfUniformCrossover(
            double crossoverProbability, RandomGenerator<Double> crossoverRandomGenerator) {
        Check.probabilityIsValid(crossoverProbability);
        this.crossoverProbability = crossoverProbability;
        this.crossoverRandomGenerator = crossoverRandomGenerator;
    }

    @Override
    public double crossoverProbability() {
        return crossoverProbability;
    }

    @Override
    public List<S> execute(List<S> solutions) {
        Check.notNull(solutions);
        Check.that(solutions.size() == 2, "There must be two parents instead of " + solutions.size());

        return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
    }

    /**
     * Perform the crossover operation.
     *
     * @param probability Crossover setProbability
     * @param parent1 The first parent
     * @param parent2 The second parent
     * @return An array containing the two offspring
     */
    public List<S> doCrossover(
            double probability, S parent1, S parent2) {

        List<S> offspring = List.of((S) parent1.copy(), (S) parent2.copy());

        if (crossoverRandomGenerator.getRandomValue() < probability) {
            var parent1Vars = parent1.variables().get(0);
            var parent2Vars = parent2.variables().get(0);
            int commonLength = Math.min(parent1Vars.length(), parent2Vars.length());

            var diffIndices = IntStream.range(0, commonLength)
                    .filter(i -> parent1Vars.get(i) != parent2Vars.get(i))
                    .toArray();

            shuffle(diffIndices, JMetalRandom.getInstance().nextInt(0, commonLength));
            int changes = diffIndices.length / 2;

            for (int i = 0; i < changes; i++) {
                int index = diffIndices[i];
                boolean temp = offspring.get(0).variables().get(0).get(index);
                offspring.get(0).variables().get(0).set(index, offspring.get(1).variables().get(0).get(index));
                offspring.get(1).variables().get(0).set(index, temp);
            }
        }
        return offspring;
    }

    @Override
    public int numberOfRequiredParents() {
        return 2;
    }

    @Override
    public int numberOfGeneratedChildren() {
        return 2;
    }
}