package com.github.finfeat4j.ml.sfa;

import com.github.finfeat4j.core.Buffer.DoubleBuffer;
import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.util.ArrayProducer;
import com.github.finfeat4j.util.Dataset;
import com.github.finfeat4j.util.FeatureIndex;
import com.github.finfeat4j.util.IndicatorSet;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import sfa.timeseries.TimeSeries;
import sfa.transformation.SFA;
import sfa.transformation.SFA.HistogramType;

public record SFATransformers(int winSize, Map<String, SFA> sfaMap) implements Serializable  {

    @SuppressWarnings(value = {"rawtypes", "unchecked"})
    public IndicatorSet<double[]> asIndicatorSet(String[] features) {
        var array = new Indicator[features.length];
        for (int i = 0; i < array.length; i++) {
            var feature = features[i];
            var transformer = sfaMap.get(feature);
            if (transformer != null) {
                array[i] = new ArrayProducer(new SFATransformer(i, winSize, transformer, feature), transformer.wordLength);
            } else {
                array[i] = new FeatureIndex(i, feature);
            }
        }
        return new IndicatorSet<>(array);
    }

    public static SFATransformers load(InputStream modelFile) {
        try {
            return (SFATransformers) new ObjectInputStream(modelFile).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static SFATransformers fit(Dataset input, HistogramType histogramType, int wordLength, int alphabetSize,
        int windowSize, File modelFile, String labelColumn, double trainRatio, String... skipColumns) {
        var toSkip = Set.of(skipColumns);
        var cols = Arrays.stream(input.features())
            .filter(Predicate.not(toSkip::contains))
            .toArray(String[]::new);
        var transformers = new ConcurrentHashMap<String, SFA>();
        var labels = input.column(labelColumn);
        var ratio = Math.max(0.05, Math.min(trainRatio, 1.0));
        Arrays.stream(cols).parallel().forEach(column -> {
            if (column.equals(labelColumn)) {
                return;
            }
            var featureVec = input.column(column);
            var sfa = new SFA(histogramType);
            var series = new ArrayList<TimeSeries>();
            int trainLen = (int) (featureVec.length * ratio);
            for (int i = 0; i < trainLen - windowSize + 1; i++) {
                var label = labels[i];
                series.add(new TimeSeries(Arrays.copyOfRange(featureVec, i, i + windowSize), label));
            }
            sfa.fitTransform(series.toArray(TimeSeries[]::new), wordLength, alphabetSize, false);
            transformers.put(column, sfa);
        });
        var transformersMap = new HashMap<>(transformers);
        var record = new SFATransformers(windowSize, transformersMap);
        try {
            var out = new ObjectOutputStream(new FileOutputStream(modelFile));
            try {
                out.writeObject(record);
            } catch (Throwable t) {
                try {
                    out.close();
                } catch (Throwable t2) {
                    t.addSuppressed(t2);
                }
                throw t;
            }
            out.close();
        } catch (IOException var7) {
            throw new RuntimeException(var7);
        }
        return record;
    }

    public static class SFATransformer implements Indicator<double[], double[]> {

        private final int featureIdx;
        private final DoubleBuffer buffer;
        private final double[] window;
        private final SFA sfa;
        private final String name;

        public SFATransformer(int featureIdx, int winSize, SFA sfa, String name) {
            this.featureIdx = featureIdx;
            this.buffer = new DoubleBuffer(winSize);
            this.window = new double[winSize];
            this.sfa = sfa;
            this.name = name;
        }

        @Override
        public double[] apply(double[] values) {
            this.buffer.addToEnd(values[this.featureIdx]);
            if (this.buffer.isFull()) {
                this.buffer.copy(window);
                var sfa = this.sfa.transform(new TimeSeries(window, -1.0));
                var result = new double[sfa.length];
                for (int i = 0; i < sfa.length; i++) {
                    result[i] = sfa[i];
                }
                return result;
            }
            return new double[4];
        }

        @Override
        public String getName(Object... attrs) {
            return Indicator.super.getName(name);
        }

        @Override
        public String simpleName() {
            return "SFA";
        }
    }
}
