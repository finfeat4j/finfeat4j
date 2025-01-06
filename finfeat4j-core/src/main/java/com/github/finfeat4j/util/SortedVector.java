package com.github.finfeat4j.util;

import java.util.Arrays;

/**
 * This implementation is not mine. I forgot the source.
 */
public class SortedVector {

    private final double[] a;

    private int n;

    private int availSlot;

    private boolean sorted;

    public SortedVector(int size) {

        a = new double[size];

        n = 0;

        availSlot = -1;

        sorted = false;
    }

    /**
     * append item value onto the end of the list.  Note that if the item is added to the last slot,
     * the list is immediately sorted into ascending numerical order afterwards as a side effect to
     * keep the logic in the other methods consistent. runtime is usually O(1), but if append is
     * used for the last item, there is a sort adding O(N*log_2(N)). For best use, append(v) the
     * first size-1 items and thereafter use insertIntoOpenSlot(v).
     *
     * @param value
     */
    public void append(double value) {

        if (n == (a.length)) {
            throw new IllegalArgumentException("0) there must be an empty slot in order to append."
                + " remove and item then try insert again or construct larger list.");
        }

        a[n] = value;

        n++;

        if (n == a.length) {

            Arrays.sort(a);

            sorted = true;
        }
    }

    /**
     * Insert the value into the list while maintaining the sorted state of the list.  Note that if
     * there is not exactly one available slot in the list, an IllegalArgumentException will be
     * thrown. runtime is usually O(log_2(N)) + less than O(N), but once per class lifetime the sort
     * may occur here adding O(N*log_2(N)).
     *
     * @param value
     */
    public void insertIntoOpenSlot(double value) {

        if (n != (a.length - 1)) {
            String err =
                "1) the method is meant to be used only on a full list." + " a.length=" + a.length
                    + " n=" + n;
            throw new IllegalArgumentException(err);
        }

        if (!sorted) {
            // this can happen if the user used "size - 1" append()s followed
            // by insertIntoOpenSlot.  It's only needed once for lifetime
            // of object.

            if (availSlot != -1) {
                throw new IllegalArgumentException(
                    "Error in the algorithm... should have been sorted already");
            }

            a[n] = value;

            n++;

            Arrays.sort(a);

            sorted = true;

            return;
        }

        int insIdx = Arrays.binarySearch(a, value);
        if (insIdx < 0) {
            insIdx *= -1;
            insIdx--;
        }

        if (insIdx == availSlot) {

            a[availSlot] = value;

        } else if (insIdx < availSlot) {

            // move all items from insIdx to availSlot down by 1
            for (int i = (availSlot - 1); i >= insIdx; i--) {
                a[i + 1] = a[i];
            }

            a[insIdx] = value;

        } else {

            int end = insIdx - 1;

            // move items up from availSlot +1 to insIdx - 1
            // then insert value into insIdx - 1
            for (int i = availSlot; i < end; i++) {
                a[i] = a[i + 1];
            }

            a[insIdx - 1] = value;
        }
        n++;
        availSlot = -1;
    }

    public int indexOf(double value) {
        return Arrays.binarySearch(a, value);
    }

    public double valueAt(int index) {
        return a[index];
    }

    /**
     * remove the item from the full list of items. runtime is O(log_2(N)). NOTE: this could be made
     * O(1) runtime complexity at the expense of 3 * space complexity.
     *
     * @param value
     */
    public void remove(double value) {

        if (n != a.length) {
            throw new IllegalArgumentException(
                "2) the method is meant to be used only on a full list." + " a.length=" + a.length
                    + " n=" + n);
        }

        int rmIdx = Arrays.binarySearch(a, value);

        if (rmIdx < 0) {
            throw new IllegalArgumentException("could not find item in list");
        }

        availSlot = rmIdx;

        // to keep the list in a state where the next binary search works,
        // set the empty slot value to the proceeding value or max integer.
        if (availSlot == (a.length - 1)) {
            a[availSlot] = Double.POSITIVE_INFINITY;
        } else {
            a[availSlot] = a[availSlot + 1];
        }

        n--;
    }

    /**
     * get median from the internal array.  Note that this will throw an IllegalArgumentException if
     * the list is not full. runtime is O(1)
     *
     * @return median
     */
    public double getMedian() {

        if (n != a.length) {
            // NOTE: in the use above, this is never invoked unless the
            // list a is full so this exception should never be thrown
            throw new IllegalArgumentException(
                "3) the method is meant to be used only on a full list." + " a.length=" + a.length
                    + " n=" + n);
        }

        int midIdx = ((n & 1) == 1) ? n / 2 : (n - 1) / 2;

        return a[midIdx];
    }

    /**
     * @return minimum value
     */
    public double getMin() {
        return a[0];
    }

    /**
     * @return maximum value
     */
    public double getMax() {
        return a[n - 1];
    }
}