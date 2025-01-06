package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.ValueBack;
import com.github.finfeat4j.ta.ma.SMA;

import java.math.BigDecimal;

public class DPO implements Indicator<BigDecimal, BigDecimal> {
  private final Indicator<BigDecimal, BigDecimal> ma;
  private final Params params;

  public DPO(int period, Indicator<BigDecimal, BigDecimal> ma) {
    this.ma = ma.then(new ValueBack<>(period / 2 + 1));
    this.params = new Params(period);
  }

  public DPO(int period) {
    this(period, new SMA(period));
  }

  @Override
  public BigDecimal apply(BigDecimal x) {
    var ma = this.ma.apply(x);
    return x.subtract(ma);
  }

  @Override
  public Params getParams() {
    return this.params;
  }
}