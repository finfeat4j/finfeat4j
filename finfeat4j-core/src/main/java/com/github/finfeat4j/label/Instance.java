package com.github.finfeat4j.label;

import com.github.finfeat4j.api.LabelProducer;

public class Instance {

    private final double price;
    private final double trendPrice;
    private final double[] x;
    private final long timestamp;
    private double weight;
    private double[] probabilities;
    private LabelProducer.Label predicted;
    private LabelProducer.Label actual;

    public Instance(double price, double trendPrice,
                    double weight, long timestamp, double[] x) {
        this.price = price;
        this.trendPrice = trendPrice;
        this.weight = weight;
        this.timestamp = timestamp;
        this.x = x;
    }

    public double price() {
        return price;
    }

    public double trendPrice() {
        return trendPrice;
    }

    public double[] x() {
        return x;
    }

    public long timestamp() {
        return timestamp;
    }

    public double[] probabilities() {
        return probabilities;
    }

    public void setProbabilities(double[] probabilities) {
        this.probabilities = probabilities;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public LabelProducer.Label predicted() {
        return predicted;
    }

    public void setPredicted(LabelProducer.Label predicted) {
        this.predicted = predicted;
    }

    public LabelProducer.Label actual() {
        return actual;
    }

    public void setActual(LabelProducer.Label actual) {
        this.actual = actual;
    }
}
