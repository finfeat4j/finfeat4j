package com.github.finfeat4j.ml.sfa;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.core.Buffer.DoubleBuffer;
import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.core.Dataset;
import com.github.finfeat4j.core.IndicatorSet;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sfa.timeseries.TimeSeries;
import sfa.transformation.SFA;
import sfa.transformation.SFA.HistogramType;

public record SFATransformers(int winSize, Map<String, SFA> sfaMap) implements Serializable  {

    @SuppressWarnings(value = {"unchecked"})
    public IndicatorSet<double[]> asIndicatorSet(String... features) {
        var array = new Indicator[features.length];
        for (int i = 0; i < array.length; i++) {
            var feature = features[i];
            var transformer = sfaMap.get(feature);
            if (transformer != null) {
                array[i] = new SFATransformer(i, winSize, transformer, feature);
            } else {
                array[i] = Indicator.of(i, feature);
            }
        }
        return new IndicatorSet<double[]>(array);
    }

    public IndicatorSet<double[]> asIndicatorSet(IndicatorSet<Bar> initial, String... features) {
        var names = initial.names();
        var newOne = new IndicatorSet<double[]>();
        var pattern = Pattern.compile(".*\\[(\\d+)\\]");
        for (int i = 0; i < names.length; i++) {
            var name = names[i];
            var transformer = sfaMap.get(name);
            var selectSet = Arrays.stream(features).filter(f -> f.contains(name)).toList();
            if (transformer != null && !selectSet.isEmpty()) {
                int renameIdx = newOne.names().length;
                int[] selectIndexes = selectSet.stream()
                        .map(pattern::matcher)
                        .filter(Matcher::find)
                        .mapToInt(m -> Integer.parseInt(m.group(1)))
                        .toArray();
                newOne.add(new SFATransformer(i, winSize, transformer, name).then(Indicator.ArrayProducer.defaultReducer(selectIndexes)));
                for (var f : selectSet) {
                    newOne.rename(renameIdx++, f);
                }
            } else if (!selectSet.isEmpty()) {
                newOne.add(Indicator.of(i, name));
            }
        }
        return newOne;
    }

    public static SFATransformers fit(Dataset input, HistogramType histogramType, int wordLength, int alphabetSize,
                                      int windowSize, File modelFile, String labelColumn, double trainRatio, String... skipColumns) {
        var toSkip = Set.of(skipColumns);
        var cols = Arrays.stream(input.features())
                .filter(Predicate.not(toSkip::contains))
                .toArray(String[]::new);
        var transformers = new ConcurrentHashMap<String, SFA>();
        var labels = input.column(labelColumn);
        var ratio = Math.max(0.5, Math.min(trainRatio, 1.0));
        Arrays.stream(cols).parallel().forEach(column -> {
            if (column.equals(labelColumn)) {
                return;
            }
            var featureVec = input.column(column);
            var sfa = new SFA(histogramType);
            var series = new ArrayList<TimeSeries>();
            int trainLen = (int) (featureVec.length * ratio);
            for (int i = 0; i < trainLen - windowSize + 1; i++) {
                series.add(new TimeSeries(Arrays.copyOfRange(featureVec, i, i + windowSize), labels[i]));
            }
            sfa.fitTransform(series.toArray(TimeSeries[]::new), wordLength, alphabetSize, false);
            transformers.put(column, sfa);
        });
        var transformersMap = new HashMap<>(transformers);
        var record = new SFATransformers(windowSize, transformersMap);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile))) {
            out.writeObject(record);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save SFATransformers to file", e);
        }
        return record;
    }

    public static SFATransformers load(File modelFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile))) {
            return (SFATransformers) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SFATransformers from file", e);
        }
    }

    public static class SFATransformer implements Indicator.ArrayProducer<double[], double[]> {

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
            return new double[sfa.wordLength];
        }

        @Override
        public String getName(Object... attrs) {
            return ArrayProducer.super.getName(name);
        }

        @Override
        public String simpleName() {
            return "SFA";
        }

        @Override
        public int size() {
            return sfa.wordLength;
        }
    }
}
