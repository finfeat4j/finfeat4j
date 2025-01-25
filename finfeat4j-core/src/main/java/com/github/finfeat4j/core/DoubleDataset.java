package com.github.finfeat4j.core;

import com.github.finfeat4j.api.Dataset;
import com.github.finfeat4j.api.Indicator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public record DoubleDataset(
    String[] features,
    Map<String, Integer> featureIndex,
    // first index is a row, second index is a feature column
    double[][] data,
    ColumnConfig columnConfig) implements Dataset<double[]> {

    public DoubleDataset {
        if (features.length != data[0].length) {
            throw new IllegalArgumentException("Number of features must match number of columns in data");
        }
    }

    public DoubleDataset(String[] features, double[][] data) {
        this(features, Dataset.computeIndex(features), data, DEFAULT);
    }

    public double[] column(int index) {
        return Arrays.stream(data)
            .mapToDouble(datum -> datum[index])
            .toArray();
    }

    @Override
    public IntFunction<double[][]> generator() {
        return double[][]::new;
    }

    @Override
    public Function<double[], double[]> reducer(int[] indices, int size) {
        return size < 0 ? Indicator.ArrayProducer.defaultReducer(indices) : Indicator.ArrayProducer.defaultReducer(indices, size);
    }

    @Override
    public Dataset<double[]> create(String[] features, double[][] data) {
        return new DoubleDataset(features, data);
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

    public static DoubleDataset load(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return load(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DoubleDataset load(InputStream is) {
        try (Reader reader = new InputStreamReader(is);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> features = csvParser.getHeaderNames();
            var data = csvParser.stream()
                    .map(record -> features.stream()
                            .mapToDouble(feature -> Double.parseDouble(record.get(feature)))
                            .toArray())
                    .toArray(double[][]::new);
            return new DoubleDataset(features.toArray(String[]::new), data);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
