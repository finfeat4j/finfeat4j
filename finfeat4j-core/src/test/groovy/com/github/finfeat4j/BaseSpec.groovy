package com.github.finfeat4j

import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.core.Dataset
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.closeTo

abstract class BaseSpec extends Specification {

    @Shared
    Instant startTime;

    public static List<Bar> BARS = BaseSpec
            .getResource("data.csv").readLines().drop(1).collect {
        def parts = it.split(",")
        new Bar.BaseBar(
            parts[0].toLong(),
            new BigDecimal(parts[1]),
            new BigDecimal(parts[2]),
            new BigDecimal(parts[3]),
            new BigDecimal(parts[4]),
            new BigDecimal(parts[5]),
            parts[6].toLong()
        )
    }

    void isCloseTo(double a, double b, double threshold = 1) {
        double th = Math.pow(10, -threshold)
        assertThat(a, closeTo(b, th))
    }

    boolean equals(int[] a, int[] b) {
        if (a.length != b.length) {
            throw new IllegalStateException("Arrays have different length");
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                throw new IllegalStateException("On index ${i} ${a[i]} != ${b[i]}")
            }
        }
        return true
    }

    boolean equals(double[] a, double[] b, int threshold = 1) {
        double th = Math.pow(10, -threshold);
        if (a.length != b.length) {
            throw new IllegalStateException("Arrays have different length");
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                assertThat("On index ${i}", a[i], closeTo(b[i], th))
            }
        }
        return true
    }

    boolean equals(String[] a, String[] b) {
        if (a.length != b.length) {
            throw new IllegalStateException("Arrays have different length");
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                throw new IllegalStateException("On index ${i} ${a[i]} != ${b[i]}")
            }
        }
        return true
    }

    boolean equals(Dataset one, Dataset another) {
        if (one.data().length != another.data().length) {
            throw new IllegalStateException("Datasets have different length");
        }
        var oneData = one.data();
        var anotherData = another.data();
        for (int i = 0; i < oneData.length; i++) {
            equals(oneData[i], anotherData[i])
            println "OK ${i}"
        }
        var oneCols = one.features();
        var anotherCols = another.features();
        equals(oneCols, anotherCols)
        return true
    }

    Dataset load(String name, Class clazz = this.getClass()) {
        return Dataset.load(clazz.getResource(name).newInputStream())
    }
}
