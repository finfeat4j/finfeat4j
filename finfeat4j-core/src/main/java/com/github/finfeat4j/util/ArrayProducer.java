package com.github.finfeat4j.util;

import com.github.finfeat4j.core.Indicator;

public interface ArrayProducer<T, R> extends Indicator<T, R> {
    int size();
}
