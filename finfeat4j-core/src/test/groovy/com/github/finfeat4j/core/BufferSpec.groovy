package com.github.finfeat4j.core

import com.github.finfeat4j.BaseSpec

class BufferSpec extends BaseSpec {

    def 'test DoubleBuffer initialization'() {
        when:
        def buffer = new Buffer.DoubleBuffer(10)

        then:
        buffer.size() == 0
        buffer.getLength() == 10
        !buffer.isFull()
    }

    def 'test DoubleBuffer addToEnd and size'() {
        given:
        def buffer = new Buffer.DoubleBuffer(3)

        when:
        buffer.addToEnd(1.0d)
        buffer.addToEnd(2.0d)
        buffer.addToEnd(3.0d)

        then:
        buffer.size() == 3
        buffer.isFull()
    }

    def 'test DoubleBuffer head and tail'() {
        given:
        def buffer = new Buffer.DoubleBuffer(3)

        when:
        buffer.addToEnd(1.0d)
        buffer.addToEnd(2.0d)
        buffer.addToEnd(3.0d)

        then:
        buffer.head() == 1.0
        buffer.tail() == 3.0
    }

    def 'test DoubleBuffer get and getR'() {
        given:
        def buffer = new Buffer.DoubleBuffer(3)

        when:
        buffer.addToEnd(1.0d)
        buffer.addToEnd(2.0d)
        buffer.addToEnd(3.0d)

        then:
        buffer.get(0) == 1.0d
        buffer.get(1) == 2.0d
        buffer.get(2) == 3.0d
        buffer.getR(0) == 3.0d
        buffer.getR(1) == 2.0d
        buffer.getR(2) == 1.0d
    }

    def 'test DoubleBuffer toStream and toStreamReversed'() {
        given:
        def buffer = new Buffer.DoubleBuffer(3)

        when:
        buffer.addToEnd(1.0d)
        buffer.addToEnd(2.0d)
        buffer.addToEnd(3.0d)

        then:
        equals(buffer.toStream().toArray(), [1.0, 2.0, 3.0] as double[])
        equals(buffer.toStreamReversed().toArray(), [3.0, 2.0, 1.0] as double[])
    }

    def 'test DoubleBuffer copy'() {
        given:
        def buffer = new Buffer.DoubleBuffer(3)
        buffer.addToEnd(1.0d)
        buffer.addToEnd(2.0d)
        buffer.addToEnd(3.0d)

        when:
        def copyArray = new Double[3]
        buffer.copy(copyArray)

        then:
        copyArray == [1.0d, 2.0d, 3.0d] as Double[]
    }

    def 'test IntBuffer initialization'() {
        when:
        def buffer = new Buffer.IntBuffer(10)

        then:
        buffer.size() == 0
        buffer.getLength() == 10
        !buffer.isFull()
    }

    def 'test IntBuffer addToEnd and size'() {
        given:
        def buffer = new Buffer.IntBuffer(3)

        when:
        buffer.addToEnd(1)
        buffer.addToEnd(2)
        buffer.addToEnd(3)

        then:
        buffer.size() == 3
        buffer.isFull()
    }

    def 'test IntBuffer head and tail'() {
        given:
        def buffer = new Buffer.IntBuffer(3)

        when:
        buffer.addToEnd(1)
        buffer.addToEnd(2)
        buffer.addToEnd(3)

        then:
        buffer.head() == 1
        buffer.tail() == 3
    }

    def 'test IntBuffer get and getR'() {
        given:
        def buffer = new Buffer.IntBuffer(3)

        when:
        buffer.addToEnd(1)
        buffer.addToEnd(2)
        buffer.addToEnd(3)

        then:
        buffer.get(0) == 1
        buffer.get(1) == 2
        buffer.get(2) == 3
        buffer.getR(0) == 3
        buffer.getR(1) == 2
        buffer.getR(2) == 1
    }

    def 'test IntBuffer toStream and toStreamReversed'() {
        given:
        def buffer = new Buffer.IntBuffer(3)

        when:
        buffer.addToEnd(1)
        buffer.addToEnd(2)
        buffer.addToEnd(3)

        then:
        equals(buffer.toStream().toArray(), [1, 2, 3] as int[])
        equals(buffer.toStreamReversed().toArray(), [3, 2, 1] as int[])
    }

    def 'test IntBuffer copy'() {
        given:
        def buffer = new Buffer.IntBuffer(3)
        buffer.addToEnd(1)
        buffer.addToEnd(2)
        buffer.addToEnd(3)

        when:
        def copyArray = new Integer[3]
        buffer.copy(copyArray)

        then:
        copyArray == [1, 2, 3] as Integer[]
    }

    def 'test ObjBuffer initialization'() {
        when:
        def buffer = new Buffer.ObjBuffer<String>(10)

        then:
        buffer.size() == 0
        buffer.getLength() == 10
        !buffer.isFull()
    }

    def 'test ObjBuffer addToEnd and size'() {
        given:
        def buffer = new Buffer.ObjBuffer<String>(3)

        when:
        buffer.addToEnd("a")
        buffer.addToEnd("b")
        buffer.addToEnd("c")

        then:
        buffer.size() == 3
        buffer.isFull()
    }

    def 'test ObjBuffer head and tail'() {
        given:
        def buffer = new Buffer.ObjBuffer<String>(3)

        when:
        buffer.addToEnd("a")
        buffer.addToEnd("b")
        buffer.addToEnd("c")

        then:
        buffer.head() == "a"
        buffer.tail() == "c"
    }

    def 'test ObjBuffer get and getR'() {
        given:
        def buffer = new Buffer.ObjBuffer<String>(3)

        when:
        buffer.addToEnd("a")
        buffer.addToEnd("b")
        buffer.addToEnd("c")

        then:
        buffer.get(0) == "a"
        buffer.get(1) == "b"
        buffer.get(2) == "c"
        buffer.getR(0) == "c"
        buffer.getR(1) == "b"
        buffer.getR(2) == "a"
    }

    def 'test ObjBuffer toStream and toStreamReversed'() {
        given:
        def buffer = new Buffer.ObjBuffer<String>(3)

        when:
        buffer.addToEnd("a")
        buffer.addToEnd("b")
        buffer.addToEnd("c")

        then:
        equals(buffer.toStream().toArray(String[]::new), ["a", "b", "c"] as String[])
        equals(buffer.toStreamReversed().toArray(String[]::new), ["c", "b", "a"] as String[])
    }

    def 'test ObjBuffer copy'() {
        given:
        def buffer = new Buffer.ObjBuffer<String>(3)
        buffer.addToEnd("a")
        buffer.addToEnd("b")
        buffer.addToEnd("c")

        when:
        def copyArray = new String[3]
        buffer.copy(copyArray)

        then:
        copyArray == ["a", "b", "c"] as String[]
    }
}
