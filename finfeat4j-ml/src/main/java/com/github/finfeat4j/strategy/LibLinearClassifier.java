package com.github.finfeat4j.strategy;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.label.Instance;
import de.bwaldvogel.liblinear.*;

import java.util.stream.IntStream;

import static moa.core.Utils.maxIndex;

public class LibLinearClassifier implements Classifier {

    private Model model;


    @Override
    public Instance[] fit(TrainTest trainTest) {
        Linear.disableDebugOutput();
        var bias = (double)1.0F;
        var solverType = SolverType.L2R_LR;
        var iterations = 5000;
        var p = 0.1;
        var c = (double)1.0F;

        var parameters = new Parameter(solverType, c, iterations, p);
        var problem = toProblem(trainTest, bias);
        this.model = Linear.train(problem, parameters);
        return trainTest.test().peek(this::predict).toArray(Instance[]::new);
    }

    @Override
    public Instance[] predict(TrainTest trainTest) {
        return trainTest.test().peek(this::predict).toArray(Instance[]::new);
    }

    private void predict(Instance instance) {
        var x = toFeatureNode(instance);
        var probability = new double[2];
        var value = Linear.predictProbability(model, x, probability);
        var clazz = maxIndex(probability);
        instance.setPredicted(LabelProducer.Label.valueOf(clazz));
        instance.setProbabilities(probability);
    }

    private Problem toProblem(TrainTest trainTest, double bias) {
        var problem = new Problem();
        problem.l = trainTest.trainSize();
        problem.n = trainTest.featureSize() + 1;
        problem.x = new FeatureNode[trainTest.trainSize()][];
        problem.y = new double[trainTest.trainSize()];
        problem.bias = bias;
        int i = 0;
        for (var instance : trainTest.train().toList()) {
            problem.x[i] = toFeatureNode(instance);
            problem.y[i] = instance.actual().code();
            i++;
        }

        return problem;
    }

    private FeatureNode[] toFeatureNode(Instance instance) {
        var x = instance.x();
        var featureNodes = new FeatureNode[x.length];
        IntStream.range(0, x.length).forEach(i -> {
            featureNodes[i] = new FeatureNode(i + 1, x[i]);
        });
        // do we need to add bias?
        return featureNodes;
    }
}
