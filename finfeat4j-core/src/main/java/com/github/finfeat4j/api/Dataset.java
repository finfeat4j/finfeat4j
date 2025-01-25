package com.github.finfeat4j.api;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Dataset<T> {

    T[] data();
    String[] features();
    Map<String, Integer> featureIndex();
    ColumnConfig columnConfig();
    T column(int index);
    IntFunction<T[]> generator();
    Function<T, T> reducer(int[] indices, int size);
    Dataset<T> create(String[] features, T[] data);

    default T column(String name) {
        var colIdx = IntStream.range(0, features().length)
            .filter(i -> features()[i].equals(name))
            .findFirst()
            .orElseThrow();
        return column(colIdx);
    };

    default int[] indexOf(String... features) {
        int[] indexes = new int[features.length];
        for (int i = 0; i < features.length; i++) {
            var feature = features[i];
            try {
                indexes[i] = featureIndex().get(feature);
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Feature not found: " + feature);
            }
        }
        return indexes;
    }

    default Dataset<T> select(String... features) {
        return select(indexOf(features));
    }

    default Dataset<T> select(int... indices) {
        var newFeatures = Arrays.stream(indices)
                .mapToObj(index -> this.features()[index])
                .toArray(String[]::new);
        var data = Arrays.stream(this.data())
                .map(reducer(indices, -1))
                .toArray(generator());
        return create(newFeatures, data);
    }

    default Dataset<T> drop(String... features) {
        return drop(indexOf(features));
    }

    default Dataset<T> drop(int... indices) {
        Set<Integer> toDrop = Arrays.stream(indices).boxed().collect(Collectors.toSet());
        var newFeatures = IntStream.range(0, this.features().length)
                .filter(i -> !toDrop.contains(i))
                .mapToObj(i -> this.features()[i])
                .toArray(String[]::new);
        var data = Arrays.stream(this.data())
                .map(reducer(indices, features().length))
                .toArray(generator());
        return create(newFeatures, data);
    }

    default int[] indexOf(Collection<String> toSelect) {
        return indexOf(toSelect.toArray(String[]::new));
    }

    default Dataset<T> merge(Dataset<T> other) {
        var newFeatures = Stream.of(Stream.of(this.features()), Stream.of(other.features()))
                .flatMap(s -> s)
                .distinct()
                .toArray(String[]::new);
        assert this.data().length == other.data().length;
        assert newFeatures.length == this.features().length + other.features().length;
        var data = IntStream.range(0, this.data().length)
                .mapToObj(i -> {
                    var xRow = this.data()[i];
                    var yRow = other.data()[i];
                    var xLen = Array.getLength(xRow);
                    var yLen = Array.getLength(yRow);
                    var newRow = new double[xLen + yLen];
                    System.arraycopy(xRow, 0, newRow, 0, xLen);
                    System.arraycopy(yRow, 0, newRow, xLen, yLen);
                    return newRow;
                })
                .toArray(generator());
        return create(newFeatures, data);
    }

    static Map<String, Integer> computeIndex(String[] features) {
        var featureIndex = new HashMap<String, Integer>();
        for (int index = 0; index < features.length; index++) {
            featureIndex.put(features[index], index);
        }
        return featureIndex;
    }


    ColumnConfig DEFAULT = new ColumnConfig("price", "trendPrice", "class");

    static String[] features(String features, ColumnConfig config) {
        Stream<String> featureStream = Arrays.stream(features.split(",\\s")).map(String::trim);
        Stream<String> configStream = Stream.of();
        if (config != null) {
            configStream = Stream.of(config.price(), config.trendPrice(), config.label());
        }
        return Stream.of(featureStream, configStream)
                .flatMap(Function.identity())
                .distinct()
                .toArray(String[]::new);
    }

    public record ColumnConfig(String price, String trendPrice, String label) {

    }
}
