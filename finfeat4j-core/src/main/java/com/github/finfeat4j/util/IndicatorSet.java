package com.github.finfeat4j.util;

import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.label.InstanceTransformer;
import com.github.finfeat4j.label.LabelProducer;
import com.github.finfeat4j.label.LabelProducer.Instance;
import com.github.finfeat4j.label.LabelProducer.OnlineLabelProducer;

import java.lang.reflect.Array;
import java.util.*;
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
        for (var indicator : indicators) {
            add(indicator);
        }
    }

    /**
     * Adds an indicator to the set.
     *
     * @param indicator the indicator to add
     */
    public void add(Indicator<T, ?> indicator) {
        indicators.computeIfAbsent(indicator.getName(), (s) -> {
            var name = indicator.getName();
            var names = new ArrayList<String>();
            indexes.add(index);
            if (indicator instanceof ArrayProducer<?, ?> producer) {
                int size = producer.size();
                for (int i = 0; i < size; i++) {
                    names.add(name + "[" + i + "]");
                    index++;
                }
            } else {
                index++;
                names.add(name);
            }
            this.names.addAll(names);
            return indicator;
        });
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
    @SuppressWarnings("unchecked")
    public Stream<double[]> transform(Stream<T> stream) {
        return stream.map(
            group(
                this.names.size(),
                indexes.stream().mapToInt(x -> x).toArray(),
                indicators.values().toArray(Function[]::new)
            )
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

    public Dataset withLabels(Stream<T> stream, LabelProducer producer, long stopId) {
        var trendPriceIdx = names.indexOf("trendPrice");
        var priceIdx = names.indexOf("price");
        var labelIdx = names.indexOf("class");

        var labelProducer = new OnlineLabelProducer(producer, stopId);
        var data = transform(stream)
            .flatMap(new InstanceTransformer(labelProducer, trendPriceIdx, priceIdx))
            .peek(i -> {
                i.x()[labelIdx] = i.getLabel().code();
            })
            .map(Instance::x)
            .toArray(double[][]::new);
        return new Dataset(this.names.toArray(String[]::new), data);
    }

    public Dataset withLabels(Stream<T> stream, LabelProducer producer) {
        return withLabels(stream, producer, -1);
    }

    public Dataset asDataset(Stream<T> stream, long skip) {
        return new Dataset(this.names.toArray(String[]::new), transform(stream).skip(skip).toArray(double[][]::new));
    }

    public Dataset asDataset(Stream<T> stream) {
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
