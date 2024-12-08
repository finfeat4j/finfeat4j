package com.github.finfeat4j.core;

import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Circular buffer implementation.
 *
 * @param <T> the type of the buffer
 * @param <S> the type of the stream it produces
 */
public abstract class Buffer<T, S extends BaseStream<T, S>> {

    protected int mCnt = 0;
    protected int mStart = 0;
    protected int mEnd = 0;
    protected final int mSize;

    public Buffer(int size) {
        this.mSize = size;
    }

    public void init() {
        mCnt = 0;
        mStart = 0;
        mEnd = 0;
    }

    public int size() {
        return Math.min(mCnt, mSize);
    }

    public boolean isFull() {
        return mCnt >= mSize;
    }

    public int getLength() {
        return mSize;
    }



    public abstract S toStream();

    public abstract S toStreamReversed();



    public abstract void copy(T[] copyArray);

    public static class ObjBuffer<T> extends Buffer<T, Stream<T>> {

        private final T[] mBuf;

        public ObjBuffer(T[] buff) {
            super(buff.length);
            this.mBuf = buff;
        }

        public ObjBuffer(int size) {
            this((T[]) new Object[size]);
        }

        public void addToEnd(T val) {
            if (mCnt < mSize) {
                mBuf[mCnt] = val;
                mEnd = mCnt;
            } else {
                mEnd = (mEnd + 1) % mSize;
                mStart = (mStart + 1) % mSize;
                mBuf[mEnd] = val;
            }
            mCnt++;
        }

        public T head() {
            return this.get(0);
        }

        public T tail() {
            return this.get(Math.min(mCnt, mSize) - 1);
        }

        public Stream<T> toStream() {
            return IntStream.range(0, Math.min(mCnt, mSize))
                .mapToObj(this::get);
        }

        public Stream<T> toStreamReversed() {
            int to = Math.min(mCnt, mSize);
            int from = 0;
            return IntStream
                .iterate(to - 1, i -> i - 1)
                .limit(to - from)
                .mapToObj(this::get);
        }

        public T get(final int i) {
            T val = null;
            if (isFull()) {
                if (i < mSize) {
                    int ix = (mStart + i) % mSize;
                    val = mBuf[ix];
                } else {
                    throw new BufferOverflowException();
                }
            } else {
                val = mBuf[i];
            }
            return val;
        }

        /**
         * Get reversed, like in trading view src[0] src[1] etc
         *
         * @param i index back
         * @return 0.0 in case if value is missing
         */
        public T getR(final int i) {
            final int t = Math.min(mCnt, mSize) - 1;
            if (t < 0 || i > t) {
                return null;
            }
            return get(Math.max(0, t - i));
        }

        public void copy(T[] copyArray) {
            for (int i = 0; i < copyArray.length; i++) {
                var value = get(i);
                if (value.getClass().isArray() && copyArray[i] != null) {
                    System.arraycopy(value, 0, copyArray[i], 0, Array.getLength(copyArray[i]));
                } else {
                    copyArray[i] = value;
                }
            }
        }
    }

    public static class DoubleBuffer extends Buffer<Double, DoubleStream> {

        private final double[] mBuf;

        public DoubleBuffer(int size) {
            super(size);
            this.mBuf = new double[size];
        }

        public void addToEnd(double val) {
            if (mCnt < mSize) {
                mBuf[mCnt] = val;
                mEnd = mCnt;
            } else {
                mEnd = (mEnd + 1) % mSize;
                mStart = (mStart + 1) % mSize;
                mBuf[mEnd] = val;
            }
            mCnt++;
        }

        public double head() {
            return get(0);
        }

        public double tail() {
            return get(Math.min(mCnt, mSize) - 1);
        }

        @Override
        public DoubleStream toStream() {
            return IntStream.range(0, Math.min(mCnt, mSize))
                .mapToDouble(this::get);
        }

        @Override
        public DoubleStream toStreamReversed() {
            int to = Math.min(mCnt, mSize);
            return IntStream.iterate(to - 1, i -> i - 1)
                .limit(to)
                .mapToDouble(this::get);
        }

        public double get(final int i) {
            if (isFull() && i >= mSize) {
                throw new BufferOverflowException();
            }
            return mBuf[(mStart + i) % mSize];
        }

        public double getR(final int i) {
            final int t = Math.min(mCnt, mSize) - 1;
            if (t < 0 || i > t) {
                throw new BufferOverflowException();
            }
            return get(Math.max(0, t - i));
        }

        @Override
        public void copy(Double[] copyArray) {
            for (int i = 0; i < copyArray.length; i++) {
                copyArray[i] = get(i);
            }
        }

        public void copy(double[] copyArray) {
            for (int i = 0; i < copyArray.length; i++) {
                copyArray[i] = get(i);
            }
        }

        public double[] copy() {
            return toStream().toArray();
        }
    }

    public static class IntBuffer extends Buffer<Integer, IntStream> {

        private final int[] mBuf;

        public IntBuffer(int size) {
            super(size);
            this.mBuf = new int[size];
        }

        public void addToEnd(int val) {
            if (mCnt < mSize) {
                mBuf[mCnt] = val;
                mEnd = mCnt;
            } else {
                mEnd = (mEnd + 1) % mSize;
                mStart = (mStart + 1) % mSize;
                mBuf[mEnd] = val;
            }
            mCnt++;
        }

        public int head() {
            return get(0);
        }

        public int tail() {
            return get(Math.min(mCnt, mSize) - 1);
        }

        @Override
        public IntStream toStream() {
            return IntStream.range(0, Math.min(mCnt, mSize))
                .map(this::get);
        }

        @Override
        public IntStream toStreamReversed() {
            int to = Math.min(mCnt, mSize);
            return IntStream.iterate(to - 1, i -> i - 1)
                .limit(to)
                .map(this::get);
        }

        public int get(final int i) {
            if (isFull() && i >= mSize) {
                throw new BufferOverflowException();
            }
            return mBuf[(mStart + i) % mSize];
        }

        public int getR(final int i) {
            final int t = Math.min(mCnt, mSize) - 1;
            if (t < 0 || i > t) {
                throw new BufferOverflowException();
            }
            return get(Math.max(0, t - i));
        }

        @Override
        public void copy(Integer[] copyArray) {
            for (int i = 0; i < copyArray.length; i++) {
                copyArray[i] = get(i);
            }
        }

        public void copy(int[] copyArray) {
            for (int i = 0; i < copyArray.length; i++) {
                copyArray[i] = get(i);
            }
        }

        public int[] copy() {
            return toStream().toArray();
        }
    }
}
