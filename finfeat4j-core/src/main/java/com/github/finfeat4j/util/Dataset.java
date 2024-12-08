package com.github.finfeat4j.util;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Dataset(
    String[] features,
    // first index is a row, second index is a feature column
    double[][] data, ColumnConfig columnConfig) {

    public Dataset(String[] features, double[][] data) {
        this(features, data, DEFAULT);
    }

    public static ColumnConfig DEFAULT = new ColumnConfig("price", "trendPrice", "class");

    public double[] column(String name) {
        var colIdx = IntStream.range(0, features.length)
            .filter(i -> features[i].equals(name))
            .findFirst()
            .orElseThrow();
        return column(colIdx);
    }

    public double[] column(int index) {
        return Arrays.stream(data)
            .mapToDouble(datum -> datum[index])
            .toArray();
    }

    public int[] indexOf(String... features) {
        Set<String> toSelect = Set.of(features);
        return IntStream.range(0, this.features.length)
            .filter(i -> toSelect.contains(this.features[i]))
            .toArray();
    }

    public Dataset drop(String... features) {
        return drop(indexOf(features));
    }

    public Dataset drop(int... indices) {
        Set<Integer> toDrop = Arrays.stream(indices).boxed().collect(Collectors.toSet());
        var newFeatures = IntStream.range(0, this.features.length)
            .filter(i -> !toDrop.contains(i))
            .mapToObj(i -> this.features[i])
            .toArray(String[]::new);
        var data = Arrays.stream(this.data)
            .map(new ArrayReducer(indices, this.data[0].length))
            .toArray(double[][]::new);
        return new Dataset(newFeatures, data);
    }

    public Dataset select(String... features) {
        return select(indexOf(features));
    }

    public Dataset select(int... indices) {
        var newFeatures = Arrays.stream(indices)
            .mapToObj(index -> this.features[index])
            .toArray(String[]::new);
        var data = Arrays.stream(this.data)
            .map(new ArrayReducer(indices))
            .toArray(double[][]::new);
        return new Dataset(newFeatures, data);
    }

    public Dataset merge(Dataset other) {
        var newFeatures = Stream.of(Stream.of(this.features), Stream.of(other.features))
            .flatMap(s -> s)
            .distinct()
            .toArray(String[]::new);
        assert this.data.length == other.data.length;
        assert newFeatures.length == this.features.length + other.features.length;
        var data = IntStream.range(0, this.data.length)
            .mapToObj(i -> {
                var xRow = this.data[i];
                var yRow = other.data[i];
                var newRow = new double[xRow.length + yRow.length];
                System.arraycopy(xRow, 0, newRow, 0, xRow.length);
                System.arraycopy(yRow, 0, newRow, xRow.length, yRow.length);
                return newRow;
            })
            .toArray(double[][]::new);
        return new Dataset(newFeatures, data);
    }

    public void save(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println(String.join(",", Arrays.stream(features).map(f -> "\"" + f + "\"").toArray(String[]::new)));
            // Write data rows
            for (double[] row : data) {
                writer.println(Arrays.stream(row)
                    .mapToObj(Double::toString)
                    .collect(Collectors.joining(",")));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Dataset load(InputStream stream) {
        var splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        var replaceQuotes = "\"";
        var replacement = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String headerLine = reader.readLine();
            String[] features = headerLine.split(splitRegex);
            double[][] data = reader.lines()
                .map(line -> line.split(splitRegex))
                .map(parts -> Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray())
                .toArray(double[][]::new);
            return new Dataset(Arrays.stream(features)
                .map(f -> f.replaceAll(replaceQuotes, replacement))
                .toArray(String[]::new), data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record ColumnConfig(String price, String trendPrice, String label) {

    }
}
