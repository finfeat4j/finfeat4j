package com.github.finfeat4j.others.quant;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Util {

  public static List<int[]> makeIntervals(int inputLength, int depth) {
    var exponent = Math.min(depth, (int) Math.floor((Math.log(inputLength) / Math.log(2))) + 1);
    var intervals = new ArrayList<int[]>();
    IntStream.range(0, exponent).mapToDouble(i -> Math.pow(2, i)).forEach(n -> {
      var indices = linspace(0, inputLength, (int) n + 1);
      var pairs = IntStream.range(0, indices.length - 1).mapToObj(j -> new int[]{indices[j], indices[j + 1]})
              .peek(intervals::add)
              .flatMapToInt(IntStream::of)
              .toArray();
      if (n > 1 && median(diff(pairs)) > 1) {
        var shift = (int) Math.ceil(inputLength / n / 2);
        for (int j = 0; j < pairs.length - 2; j += 2) {
          intervals.add(new int[]{pairs[j] + shift, pairs[j + 1] + shift});
        }
      }
    });
    return intervals;
  }

  private static int[] linspace(int start, int end, int num) {
    int[] result = new int[num];
    double step = (double) (end - start) / (num - 1);
    for (int i = 0; i < num; i++) {
      result[i] = (int) (start + i * step);
    }
    return result;
  }

  private static int[] diff(int[] array) {
    return IntStream.iterate(0, i -> i <= array.length - 1, i -> i + 2)
      .map(i -> array[i + 1] - array[i])
      .toArray();
  }

  public static double[] diff(double[] array) {
    double[] result = new double[array.length - 1];
    for (int i = 0; i < array.length - 1; i++) {
      result[i] = array[i + 1] - array[i];
    }
    return result;
  }

  public static double[] pad(double[] input, int padLeft, int padRight) {
    double[] result = new double[input.length + padLeft + padRight];
    System.arraycopy(input, 0, result, padLeft, input.length);
    for (int i = 0; i < padLeft; i++) {
      result[i] = input[0];
    }
    for (int i = 0; i < padRight; i++) {
      result[input.length + padLeft + i] = input[input.length - 1];
    }
    return result;
  }

  public static double[] avgPool1d(double[] input, int poolSize) {
    int length = input.length;
    double[] result = new double[length - poolSize + 1];
    for (int i = 0; i < length - poolSize + 1; i++) {
      double sum = 0;
      for (int j = i; j < i + poolSize; j++) {
        sum += input[j];
      }
      result[i] = sum / poolSize;
    }
    return result;
  }

  public static double[] forwardFft(double[] data) {
    double[] padded = Arrays.copyOf(data, Util.padSize(data.length));
    var transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    return Arrays.stream(transformer.transform(padded, TransformType.FORWARD))
      .mapToDouble(Complex::abs).limit((padded.length / 2) + 1).toArray();
  }

  public static int padSize(int n) {
    return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
  }

  private static double median(int[] array) {
    Arrays.sort(array);
    int middle = array.length / 2;
    if (array.length % 2 == 1) {
      return array[middle];
    } else {
      return (array[middle - 1] + array[middle]) / 2.0;
    }
  }

  public static double mean(double[] X) {
    double sum = 0.0;
    for (double x : X) {
      sum += x;
    }
    return sum / X.length;
  }

  public static double[] f_quantile(double[] X, int div) {
    int n = X.length;

    if (n <= 1) {
      return X;
    } else {
      Arrays.sort(X); // note assumption is that we can sort X, which is a copy of the original data
      int num_quantiles = 1 + (n - 1) / div;
      double[] quantiles = new double[num_quantiles];
      if (num_quantiles == 1) {
        quantiles[0] = median(X);
      } else {
        for (int i = 0; i < num_quantiles; i++) {
          if (i % 2 == 1) {
            quantiles[i] -= mean(X);
          } else {
            double q = (double) i / (num_quantiles - 1);
            quantiles[i] = quantile(X, q);
          }
        }
      }

      return quantiles;
    }
  }

  public static double median(double[] X) {
    // note X is already sorted
    int n = X.length;
    if (n % 2 == 0) {
      return (X[n / 2 - 1] + X[n / 2]) / 2.0;
    } else {
      return X[n / 2];
    }
  }

  public static double quantile(double[] X, double q) {
    // note X is already sorted
    int n = X.length;
    double pos = q * (n - 1) + 1;
    int floor = (int) Math.floor(pos);
    int ceil = (int) Math.ceil(pos);

    // Ensure floor and ceil are within array bounds
    floor = Math.max(1, Math.min(n, floor));
    ceil = Math.max(1, Math.min(n, ceil));

    if (ceil == floor) {
      return X[floor - 1];
    } else {
      return X[floor - 1] * (ceil - pos) + X[ceil - 1] * (pos - floor);
    }
  }
}