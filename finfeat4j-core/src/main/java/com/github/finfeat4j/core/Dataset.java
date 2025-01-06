package com.github.finfeat4j.core;

import com.github.finfeat4j.api.Indicator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Dataset(
    String[] features,
    Map<String, Integer> featureIndex,
    // first index is a row, second index is a feature column
    double[][] data,
    ColumnConfig columnConfig) {

    public Dataset(String[] features, double[][] data) {
        this(features, computeIndex(features), data, DEFAULT);
    }

    public static Map<String, Integer> computeIndex(String[] features) {
        var featureIndex = new HashMap<String, Integer>();
        for (int index = 0; index < features.length; index++) {
            featureIndex.put(features[index], index);
        }
        return featureIndex;
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
        int[] indexes = new int[features.length];
        for (int i = 0; i < features.length; i++) {
            var feature = features[i];
            try {
                indexes[i] = featureIndex.get(feature);
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Feature not found: " + feature);
            }
        }
        return indexes;
    }

    public int[] indexOf(Collection<String> toSelect) {
        return indexOf(toSelect.toArray(String[]::new));
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
            .map(Indicator.ArrayProducer.defaultReducer(indices, this.data[0].length))
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
            .map(Indicator.ArrayProducer.defaultReducer(indices))
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
        save(file, Stream.of(data), features);
    }

    public static void save(File file, Stream<double[]> rows, String[] features) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            // Write header
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(features);
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                // Write data rows
                rows.forEach(row -> {
                    try {
                        csvPrinter.printRecord(Arrays.stream(row).boxed().toArray());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Dataset load(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return load(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Dataset load(InputStream is) {
        try (Reader reader = new InputStreamReader(is);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> features = csvParser.getHeaderNames();
            double[][] data = csvParser.stream()
                    .map(record -> features.stream()
                            .mapToDouble(feature -> Double.parseDouble(record.get(feature)))
                            .toArray())
                    .toArray(double[][]::new);

            return new Dataset(features.toArray(String[]::new), data);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String[] features(String features, ColumnConfig config) {
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
