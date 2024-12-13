package com.github.finfeat4j.util;


public class ArrayReducer implements ArrayProducer<double[], double[]> {
    private final int[] selectedIndices;

    /**
     * This constructor creates a reducer that selects the elements with the indices specified in the selectedIndices array.
     * @param selectedIndices indices of the elements to select
     */
    public ArrayReducer(int[] selectedIndices) {
        this.selectedIndices = selectedIndices;
    }

    /**
     * This constructor creates a reducer that drops the elements with the indices specified in the toDrop array.
     * @param toDrop indices of the elements to drop
     * @param total total number of elements in the array
     */
    public ArrayReducer(int[] toDrop, int total) {
        this.selectedIndices = new int[total - toDrop.length];
        int j = 0;
        for (int i = 0; i < total; i++) {
            if (j < toDrop.length && toDrop[j] == i) {
                j++;
            } else {
                selectedIndices[i - j] = i;
            }
        }
    }

    @Override
    public double[] apply(double[] values) {
        var result = new double[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            result[i] = values[selectedIndices[i]];
        }
        return result;
    }

    @Override
    public int size() {
        return selectedIndices.length;
    }
}
