package com.github.finfeat4j.ta;

import com.github.finfeat4j.api.Indicator;
import com.github.finfeat4j.helpers.MinMax;
import com.github.finfeat4j.ta.ma.EMA;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schaff Trend Cycle (STC) Indicator
 */
public class STC implements Indicator<BigDecimal, BigDecimal> {

  private final Indicator<BigDecimal, BigDecimal> fastMa;
  private final Indicator<BigDecimal, BigDecimal> slowMa;
  private final MinMax<BigDecimal> cycleMinMax;
  private final MinMax<BigDecimal> smoothMinMax;
  private final Params params;
  private BigDecimal prevStc = BigDecimal.ZERO;
  private BigDecimal stcFinal = BigDecimal.ZERO;
  private BigDecimal smooth1 = null;
  private BigDecimal smooth2 = BigDecimal.ZERO;

  private final BigDecimal smoothingFactor;

  public STC(int cycleLength, int fastLength, int slowLength, double smoothingFactor) {
    this(cycleLength, smoothingFactor, new EMA(fastLength), new EMA(slowLength), new Params(cycleLength, fastLength, slowLength, smoothingFactor));
  }

  public STC(int cycleLength, double smoothingFactor, Indicator<BigDecimal, BigDecimal> fastMa,
             Indicator<BigDecimal, BigDecimal> slowMa, Params params) {
    this.params = params;
    this.cycleMinMax = new MinMax<>(cycleLength);
    this.smoothMinMax = new MinMax<>(cycleLength);
    this.fastMa = fastMa;
    this.slowMa = slowMa;
    this.smoothingFactor = BigDecimal.valueOf(smoothingFactor);
  }

  @Override
  public BigDecimal apply(BigDecimal value) {
    var maDiff = fastMa.apply(value).subtract(slowMa.apply(value));
    var minMax = cycleMinMax.apply(maDiff);
    var lowermost = minMax.min();
    var range = minMax.max().subtract(lowermost);
    var stcValue = this.prevStc;
    if (range.compareTo(BigDecimal.ZERO) != 0) {
      stcValue = maDiff.subtract(lowermost)
              .divide(range, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
      this.prevStc = stcValue;
    }
    if (smooth1 == null) {
      smooth1 = stcValue;
    } else {
      smooth1 = smooth1.add(smoothingFactor.multiply(stcValue.subtract(smooth1)));
    }
    var smooth1MinMax = smoothMinMax.apply(smooth1);
    var smooth1Range = smooth1MinMax.max().subtract(smooth1MinMax.min());
    if (smooth1Range.compareTo(BigDecimal.ZERO) != 0) {
      smooth2 = smooth1.subtract(smooth1MinMax.min())
              .divide(smooth1Range, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    if (stcFinal == null) {
      stcFinal = smooth2;
    } else {
      stcFinal = stcFinal.add(smoothingFactor.multiply(smooth2.subtract(stcFinal)));
    }
    return stcFinal;
  }

  @Override
  public Params getParams() {
    return this.params;
  }
}