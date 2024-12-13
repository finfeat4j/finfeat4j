package com.github.finfeat4j.others.quant;

import java.util.List;
import java.util.stream.DoubleStream;

import static com.github.finfeat4j.others.quant.Util.*;


public class QuantTransformer {

  private final List<IntervalModel> models;

  public QuantTransformer(int depth, int div) {
    this(List.of(
      new IntervalModel(depth, div, X -> X),
      new IntervalModel(depth, div, X -> avgPool1d(pad(diff(X), 2, 2), 5)),
      new IntervalModel(depth, div, X -> diff(diff(X))),
      new IntervalModel(depth, div, Util::forwardFft)
    ));
  }

  public QuantTransformer(List<IntervalModel> models) {
    this.models = models;
  }

  public double[] transform(double[] X) {
    return models.stream()
      .flatMapToDouble(intervalModel -> DoubleStream.of(intervalModel.transform(X)))
      .toArray();
  }
}
