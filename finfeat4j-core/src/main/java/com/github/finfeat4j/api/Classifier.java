package com.github.finfeat4j.api;

import com.github.finfeat4j.label.Instance;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Classifier {

    Function<double[], int[]> TO_INT = (d) -> {
        int[] ints = new int[d.length];
        for (int i = 0; i < d.length; i++) {
            ints[i] = (int) d[i];
        }
        return ints;
    };

    Instance[] fit(TrainTest trainTest);
    Instance[] predict(TrainTest trainTest);

    record TrainTest(
        Stream<Instance> train,
        int trainSize,
        Stream<Instance> test,
        int testSize,
        int featureSize
    ) {}

    public record IntDataset(int[][] x, int[] y, Instance[] instances) {
        public static IntDataset from(Stream<Instance> stream, int size) {
            var train = new int[size][];
            var trainLabels = new int[size];
            var instances = new Instance[size];
            int i = 0;
            for (var instance : (Iterable<Instance>) stream::iterator) {
                var x = TO_INT.apply(instance.x());
                train[i] = x;
                if (instance.actual() != null) {
                    trainLabels[i] = instance.actual().code();
                } else {
                    trainLabels[i] = -1;
                }
                instances[i] = instance;
                i++;
            }
            return new IntDataset(train, trainLabels, instances);
        }
    }

    public record DoubleDataset(double[][] x, int[] y, Instance[] instances) {
        public static DoubleDataset from(Stream<Instance> stream, int size) {
            var train = new double[size][];
            var trainLabels = new int[size];
            var instances = new Instance[size];
            int i = 0;
            for (var instance : (Iterable<Instance>) stream::iterator) {
                train[i] = instance.x();
                if (instance.actual() != null) {
                    trainLabels[i] = instance.actual().code();
                } else {
                    trainLabels[i] = -1;
                }
                instances[i] = instance;
                i++;
            }
            return new DoubleDataset(train, trainLabels, instances);
        }
    }

    interface ValidationMetric<T> extends Indicator<Instance, T> {

        default boolean shouldSkip(Instance prediction) {
            return prediction.actual() == null;
        }

        default T apply(Instance prediction) {
            if (shouldSkip(prediction)) {
                return last();
            }
            T last = compute(prediction);
            setLast(last);
            return last;
        }

        T last();

        void setLast(T last);

        T compute(Instance prediction);

        default double round(double value) {
            if (Double.isFinite(value)) {
                return round(value, 4).doubleValue();
            }
            return value;
        };

        boolean[] MAXIMIZE = new boolean[] { true };
        boolean[] MINIMIZE = new boolean[] { false };

        default boolean[] maximize() {
            return MAXIMIZE;
        };
    }
}