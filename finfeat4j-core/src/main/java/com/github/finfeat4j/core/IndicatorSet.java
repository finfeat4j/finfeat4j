package com.github.finfeat4j.core;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.api.LabelProducer.OnlineLabelProducer;
import com.github.finfeat4j.label.Instance;
import com.github.finfeat4j.label.InstanceTransformer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Helper class to group indicators and transform a stream of bars.
 */
public class IndicatorSet<T> {
    /**
     * Indicators to be grouped.
     */
    private final Map<String, Indicator<T, ?>> indicators = new LinkedHashMap<>();
    /**
     * Names of the indicators.
     */
    private final List<String> names = new ArrayList<>();
    /**
     * Indexes of where to put the value into for indicator, internal usage only.
     */
    private final List<Integer> indexes = new ArrayList<>();
    private int index = 0;

    @SafeVarargs
    public IndicatorSet(Indicator<T, ?>... indicators) {
        add(indicators);
    }

    /**
     * Adds an indicator to the set.
     *
     * @param indicators the indicators to add
     */
    @SafeVarargs
    public final IndicatorSet<T> add(Indicator<T, ?>... indicators) {
        for (var indicator : indicators) {
            this.indicators.computeIfAbsent(indicator.getName(), (s) -> {
                var name = indicator.getName();
                var names = new ArrayList<String>();
                indexes.add(index);
                if (indicator instanceof Indicator.ArrayProducer<?, ?> producer) {
                    int size = producer.size();
                    var arrNames = producer.arrayNames();
                    for (int i = 0; i < size; i++) {
                        var newName = name + "[" + i + "]";
                        if (arrNames != null && i < arrNames.length) {
                            newName = name + "[" + arrNames[i] + "]";
                        }
                        names.add(newName);
                        index++;
                    }
                } else {
                    index++;
                    names.add(name);
                }
                this.names.addAll(names);
                onAdd(indicator);
                return indicator;
            });
        }
        return this;
    }

    protected void onAdd(Indicator<T, ?> indicator) {
        // no-op
    }

    public void rename(int index, String name) {
        names.set(index, name);
    }

    /**
     * Transforms a stream of bars into a stream of double arrays.
     *
     * @param stream of bars
     * @return a stream of indicator values
     */
    public Stream<double[]> transform(Stream<T> stream) {
        return stream.map(transform());
    }

    @SuppressWarnings("unchecked")
    public Function<T, double[]> transform() {
        return group(
            this.names.size(),
            indexes.stream().mapToInt(x -> x).toArray(),
            indicators.values().toArray(Function[]::new)
        );
    }

    public IndicatorSet<T> filter(String... names) {
        var result = new IndicatorSet<T>();
        for (var feature : names) {
            for (var indicator : indicators.keySet()) {
                if (feature.contains(indicator)) {
                    result.add(indicators.get(indicator));
                    break;
                }
            }
        }
        return result;
    }

    public DoubleDataset withLabels(Stream<T> stream, LabelProducer producer, long stopId) {
        var data = toInstanceStream(stream, producer, stopId)
            .map(Instance::x)
            .toArray(double[][]::new);
        return new DoubleDataset(this.names.toArray(String[]::new), data);
    }

    public Stream<Instance> toInstanceStream(Stream<T> stream, LabelProducer producer, long stopId) {
        var trendPriceIdx = names.indexOf("trendPrice");
        var priceIdx = names.indexOf("price");
        var labelIdx = names.indexOf("class");
        var instanceTransformer = new InstanceTransformer(new OnlineLabelProducer(producer, stopId), trendPriceIdx, priceIdx, labelIdx);
        return transform(stream)
                .flatMap(t -> instanceTransformer.apply(t).train());
    }

    public DoubleDataset withLabels(Stream<T> stream, LabelProducer producer) {
        return withLabels(stream, producer, -1);
    }

    public DoubleDataset asDataset(Stream<T> stream, long skip) {
        return new DoubleDataset(this.names.toArray(String[]::new), transform(stream).skip(skip).toArray(double[][]::new));
    }

    public DoubleDataset asDataset(Stream<T> stream) {
        return this.asDataset(stream, 0);
    }

    /**
     * @return the names of the indicators
     */
    public String[] names() {
        return names.toArray(String[]::new);
    }

    /**
     * Group indicators into a single array.
     *
     * @param totalSize  the size of the resulting array
     * @param indicators the indicators to group
     * @param <C>        input type, usually a Bar instance
     * @return a function that groups the indicators into an array
     */
    @SafeVarargs
    static <C> Function<C, double[]> group(int totalSize, int[] indexes, Function<C, Object>... indicators) {
        return (s) -> {
            var result = new double[totalSize];
            IntStream.range(0, indicators.length)
                .parallel()
                .forEach(i -> {
                    var value = indicators[i].apply(s);
                    var startIdx = indexes[i];
                    if (isArray(value)) {
                        int length = Array.getLength(value);
                        for (int j = 0; j < length; j++) {
                            result[startIdx + j] = toDouble(Array.get(value, j));
                        }
                    } else {
                        result[startIdx] = toDouble(value);
                    }
                });
            return result;
        };
    }

    private static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    /**
     * Takes double values from a number.
     *
     * @param o the object to convert
     * @return the double value
     */
    private static double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o == null) {
            return 0.0d;
        }
        throw new RuntimeException("Unexpected value " + o);
    }
}
