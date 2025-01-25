package com.github.finfeat4j.classifier;

import com.github.finfeat4j.api.Classifier;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.label.Instance;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.FastVector;
import moa.core.InstanceExample;
import moa.core.Utils;
import moa.learners.Learner;
import moa.options.ClassOption;

import java.util.Arrays;

public class MoaClassifier implements Classifier {

    private final Learner<InstanceExample> classifier;
    private InstancesHeader header;
    private final boolean nominal;

    @SuppressWarnings("unchecked")
    public MoaClassifier(String classifier, boolean useNominal) {
        this.nominal = useNominal;
        this.classifier = (Learner<InstanceExample>) new ClassOption("learner", 'l', "Classifier to use.", moa.classifiers.Classifier.class, classifier)
            .materializeObject(null, null);
    }

    @Override
    public Instance[] fit(TrainTest trainTest) {
        var attrVec = new FastVector<Attribute>();
        for (int i = 0; i < trainTest.featureSize(); i++) {
            // adds nominal attribute, most of the classifiers does not use values
            attrVec.addElement(nominal ? new Attribute("attr" + i, Arrays.asList("0", "1")) : new Attribute("attr" + i));
        }
        attrVec.add(new Attribute("class", Arrays.asList("0", "1")));
        var instances = new Instances("TEMP", attrVec, 0);
        instances.setClassIndex(instances.numAttributes() - 1);
        this.header = new InstancesHeader(instances);
        classifier.setModelContext(header);
        classifier.prepareForUse();
        return trainAndPredict(trainTest);
    }

    @Override
    public Instance[] predict(TrainTest trainTest) {
        return trainAndPredict(trainTest);
    }

    private Instance[] trainAndPredict(TrainTest trainTest) {
        trainTest.train().forEach(i -> classifier.trainOnInstance(toExample(i, true)));
        /*return Stream.of(Stream.of(trained), trainTest.test())
                .flatMap(s->s)
                .map(this::predict)
                .toArray(Instance[]::new);*/
        return trainTest.test().map(this::predict).toArray(Instance[]::new);
    }

    private Instance predict(Instance instance) {
        var inst = toExample(instance, false);
        var votes = classifier.getPredictionForInstance(inst).getVotes();
        var probabilities = toProbability(votes);
        var prediction = Utils.maxIndex(probabilities);
        instance.setPredicted(LabelProducer.Label.valueOf(prediction));
        instance.setProbabilities(probabilities);
        return instance;
    }

    private InstanceExample toExample(Instance instance, boolean train) {
        var inst = new DenseInstance(1.0, Arrays.copyOf(instance.x(), instance.x().length + 1));
        inst.setDataset(this.header);
        if (instance.actual() != null) {
            inst.setClassValue(instance.actual().code());
        } else if (train) {
            throw new IllegalArgumentException("Actual label is required for training");
        } else {
            inst.setClassValue(-1);
        }
        inst.setDataset(header);
        return new InstanceExample(inst);
    }

    private static double[] toProbability(double[] prediction) {
        double sum = Utils.sum(prediction);
        return Arrays.stream(prediction).map(p -> p / sum).toArray();
    }
}
