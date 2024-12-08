package com.github.finfeat4j.util;

import java.util.function.Function;

public class ToIntArrayConverter implements Function<double[], int[]> {

        @Override
        public int[] apply(double[] doubles) {
            int[] ints = new int[doubles.length];
            for (int i = 0; i < doubles.length; i++) {
                ints[i] = (int) doubles[i];
            }
            return ints;
        }
}
