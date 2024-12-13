package com.github.finfeat4j.others.quant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import static com.github.finfeat4j.others.quant.Util.f_quantile;

public class IntervalModel {
  private final List<int[]> intervals = new ArrayList<>();

  private final int div;
  private final int depth;
  private final Function<double[], double[]> transform;

  public IntervalModel(int depth, int div, Function<double[], double[]> transform) {
    this.div = div;
    this.depth = depth;
    this.transform = transform;
  }

  public double[] transform(double[] X) {
    double[] transformed = transform.apply(X);
    if (intervals.isEmpty()) {
      intervals.addAll(Util.makeIntervals(transformed.length, this.depth));
    }
    return intervals.stream()
      .flatMapToDouble(interval -> this.transformInterval(interval, transformed))
      .toArray();
  }

  private DoubleStream transformInterval(int[] interval, double[] X) {
    return DoubleStream.of(f_quantile(Arrays.copyOfRange(X, interval[0], interval[1]), div));
  }
}
