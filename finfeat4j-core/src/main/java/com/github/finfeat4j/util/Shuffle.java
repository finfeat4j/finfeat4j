package com.github.finfeat4j.util;

import java.util.Random;

/**
 * Fast in-place shuffle of arrays using seed
 */
public class Shuffle {
    public static void shuffle(Object[] samples, long seed) {
        var r = new Random(seed);
        for (int i = 0; i < samples.length - 1; i++) {
            int si = i + r.nextInt(samples.length - i);
            if (si > i) {
                Object tmp = samples[i];
                samples[i] = samples[si];
                samples[si] = tmp;
            }
        }
    }

    public static void shuffle(double[] samples, long seed) {
        var r = new Random(seed);
        for (int i = 0; i < samples.length - 1; i++) {
            int si = i + r.nextInt(samples.length - i);
            if (si > i) {
                double tmp = samples[i];
                samples[i] = samples[si];
                samples[si] = tmp;
            }
        }
    }

    public static void shuffle(int[] samples, long seed) {
        var r = new Random(seed);
        for (int i = 0; i < samples.length - 1; i++) {
            int si = i + r.nextInt(samples.length - i);
            if (si > i) {
                int tmp = samples[i];
                samples[i] = samples[si];
                samples[si] = tmp;
            }
        }
    }
}
