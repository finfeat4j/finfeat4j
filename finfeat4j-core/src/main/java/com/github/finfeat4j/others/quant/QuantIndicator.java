package com.github.finfeat4j.others.quant;

import com.github.finfeat4j.core.Buffer;
import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.util.ArrayProducer;

import java.math.BigDecimal;

public class QuantIndicator implements ArrayProducer<BigDecimal, double[]> {
  private final Buffer.DoubleBuffer buffer;
  private final int tsLen;

  private final QuantTransformer transformer;

  private final Indicator.Params params;

  public QuantIndicator(int length, int depth, int div) {
    this.params = new Params(length, depth, div);
    this.buffer = new Buffer.DoubleBuffer(length);
    this.transformer = new QuantTransformer(depth, div);
    this.tsLen = this.transformer.transform(new double[length]).length;
  }

  public QuantIndicator(int length) {
    this(length, 6, 4);
  }

  @Override
  public Params getParams() {
    return this.params;
  }

  @Override
  public double[] apply(BigDecimal v) {
    this.buffer.addToEnd(v.doubleValue());
    if (this.buffer.isFull()) {
      return transformer.transform(buffer.copy());
    }
    return new double[tsLen];
  }

  @Override
  public int size() {
    return tsLen;
  }
}