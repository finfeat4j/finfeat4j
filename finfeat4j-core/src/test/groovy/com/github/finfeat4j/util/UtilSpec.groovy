package com.github.finfeat4j.util

import com.github.finfeat4j.BaseSpec
import com.github.finfeat4j.api.Bar
import com.github.finfeat4j.api.Indicator.Wrapper
import com.github.finfeat4j.core.DoubleDataset
import com.github.finfeat4j.core.IndicatorSet
import com.github.finfeat4j.helpers.bar.Close
import com.github.finfeat4j.helpers.bar.Low
import com.github.finfeat4j.stats.Stats
import com.github.finfeat4j.ta.ma.SMA

class UtilSpec extends BaseSpec {

    def 'test indicator set'() {
        when:
        def doubleProducer = new Wrapper<>((Bar bar) -> new double[]{5.0, 6.0, 7.0}, "double1")
            .array(3)
        def intProducer = new Wrapper<>((Bar bar) -> new int[]{1, 2, 3}, "int1")
            .array(3)
        def set = new IndicatorSet<>(
            new Close().then(new SMA(3)),
            new Close().then(new SMA(6)),
            new Low().then(new SMA(6)),
            new Close().then(new SMA(8))
                .then(new Stats(10))
                .then("MAD", (r) -> r.mad()),
            doubleProducer,
            intProducer
        )
        //tic()
        def transformed = set.transform(BARS.stream())
                .toArray(double[][]::new)
        //toc()
        def names = set.names()
        def lastIdx = transformed.length - 1

        then:
        names.size() == 10
        names[0] == "SMA(3,Close)"
        names[1] == "SMA(6,Close)"
        names[2] == "SMA(6,Low)"
        names[3] == "MAD(Stats(10,SMA(8,Close)))"
        names[4] == "double1[0]"
        names[5] == "double1[1]"
        names[6] == "double1[2]"
        names[7] == "int1[0]"
        names[8] == "int1[1]"
        names[9] == "int1[2]"
        transformed[0].length == 10
        transformed[lastIdx].length == 10

        transformed[0][4] == 5
        transformed[0][5] == 6
        transformed[0][6] == 7

        transformed[0][7] == 1
        transformed[0][8] == 2
        transformed[0][9] == 3

        transformed[lastIdx][4] == 5
        transformed[lastIdx][5] == 6
        transformed[lastIdx][6] == 7

        transformed[lastIdx][7] == 1
        transformed[lastIdx][8] == 2
        transformed[lastIdx][9] == 3
    }

    def 'test dataset'() {
        given:
        def dataset1 = new DoubleDataset(new String[]{"a", "b", "c"},
            new double[][]{
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            }
        )
        def dataset2 = new DoubleDataset(new String[]{"d", "e", "f"},
            new double[][]{
                [10, 11, 12],
                [13, 14, 15],
                [16, 17, 18]
            }
        )

        when: 'verify column by index'
        def col1 = dataset1.column(0)
        def col2 = dataset2.column(2)

        then:
        equals(col1, [1, 4, 7] as double[])
        equals(col2, [12, 15, 18] as double[])

        when: 'verify column by name'
        col1 = dataset1.column("b")
        col2 = dataset2.column("d")

        then:
        equals(col1, [2, 5, 8] as double[])
        equals(col2, [10, 13, 16] as double[])

        when: 'verify select by index'
        def selected = dataset1.select(0, 2)
        def selected2 = dataset2.select(1)

        then:
        equals(selected.features(), ["a", "c"] as String[])
        equals(selected.data()[0], [1, 3] as double[])
        equals(selected2.features(), ["e"] as String[])
        equals(selected2.data()[0], [11] as double[])

        when: 'verify select by name'
        selected = dataset1.select("a", "c")
        selected2 = dataset2.select("e")

        then:
        equals(selected.features(), ["a", "c"] as String[])
        equals(selected.data()[0], [1, 3] as double[])
        equals(selected2.features(), ["e"] as String[])
        equals(selected2.data()[0], [11] as double[])

        when: 'verify merge'
        def merged = dataset1.merge(dataset2)

        then:
        equals(merged.features(), ["a", "b", "c", "d", "e", "f"] as String[])
        equals(merged.data()[0], [1, 2, 3, 10, 11, 12] as double[])

        when: 'verify drop by name'
        def dropped = dataset1.drop("a", "c")

        then:
        equals(dropped.features(), ["b"] as String[])
        equals(dropped.data()[0], [2] as double[])

        when: 'verify drop by index'
        dropped = dataset1.drop(0, 2)

        then:
        equals(dropped.features(), ["b"] as String[])
        equals(dropped.data()[0], [2] as double[])
    }

    def 'test dataset save and load'() {
        given:
        def dataset = new DoubleDataset(new String[]{"a(1,2,3)", "b", "c"},
            new double[][]{
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            }
        )
        def file = File.createTempFile("dataset", "csv")

        when:
        dataset.save(file)
        def loaded = DoubleDataset.load(file.newInputStream())

        then:
        equals(dataset, loaded)

        cleanup:
        file.delete()
    }
}
