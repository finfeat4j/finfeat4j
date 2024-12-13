package com.github.finfeat4j.label;

import com.github.finfeat4j.core.Indicator;
import com.github.finfeat4j.label.LabelProducer.Result;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface LabelProducer extends Indicator<BigDecimal, Result[]> {

    enum Label {
        BUY(0),
        SELL(1),
        BUY_OVERSHOOT(2),
        SELL_OVERSHOOT(3),
        UNKNOWN(Integer.MIN_VALUE)
        ;

        private final int code;

        Label(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public static Label valueOf(int code) {
            return switch (code) {
                case 0 -> BUY;
                case 1 -> SELL;
                case 2 -> BUY_OVERSHOOT;
                case 3 -> SELL_OVERSHOOT;
                // this should not happen, but some classifiers return unknown labels :(
                default -> UNKNOWN;
            };
        }
    }

    record Result(long id, BigDecimal price, Label label) {

    }

    /**
     * This class allows to hold a buffer of instances and once labels are available, it will
     * return labeled instances back. Helpful in online learning.
     */
    class OnlineLabelProducer implements Indicator<Instance, Instance[]> {

        private final LabelProducer producer;
        private final ArrayList<Instance> buffer = new ArrayList<>();
        private Result reversal;
        private final long stopOnId;

        public OnlineLabelProducer(LabelProducer producer) {
            this(producer, -1);
        }

        public OnlineLabelProducer(LabelProducer producer, long stopOnId) {
            this.producer = producer;
            this.stopOnId = stopOnId;
        }

        @Override
        public Instance[] apply(Instance instance) {
            this.buffer.addLast(instance);
            if (this.stopOnId > 0 && instance.timestamp() >= this.stopOnId && this.reversal != null) {
                return labelTail();
            }
            var reversals = this.producer.apply(BigDecimal.valueOf(instance.trendPrice()));
            var instances = new ArrayList<Instance>();
            for (Result reversal : reversals) {
                if (!reversal.equals(this.reversal) && !this.buffer.isEmpty()) {
                    var r = this.reversal != null ? this.reversal : reversal;
                    var toTrain = new ArrayList<Instance>();
                    while (!this.buffer.isEmpty()
                        && this.buffer.getFirst().timestamp() < reversal.id()) {
                        var removed = this.buffer.removeFirst();
                        removed.setLabel(r.label());
                        removed.setWeight(1.0);
                        toTrain.add(removed);
                    }
                    instances.addAll(toTrain);
                }
                this.reversal = reversal;
            }
            return instances.toArray(Instance[]::new);
        }

        /**
         * Basic condition to label the tail of the buffer.
         * Depends on the label producer implementation actually
         */
        protected Instance[] labelTail() {
            for (var i : this.buffer) {
                i.setLabel(this.reversal.label() == Label.BUY ? Label.SELL : Label.BUY);
                i.setWeight(1.0);
            }
            return this.buffer.toArray(Instance[]::new);
        }

        public List<Instance> getBuffer() {
            return buffer;
        }

        public Result getReversal() {
            return reversal;
        }
    }

    class Instance {

        private final double price;
        private final double trendPrice;
        private final double[] x;
        private final long timestamp;
        private double weight;
        private double[] prediction;
        private Label label;

        public Instance(double price, double trendPrice,
            double weight, long timestamp, double[] x) {
            this.price = price;
            this.trendPrice = trendPrice;
            this.weight = weight;
            this.timestamp = timestamp;
            this.x = x;
        }

        public Instance(double price, Label label, long timestamp) {
            this.price = price;
            this.label = label;
            this.timestamp = timestamp;
            this.trendPrice = Double.NaN;
            this.x = null;
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

        public double[] getPrediction() {
            return prediction;
        }

        public void setPrediction(double[] prediction) {
            this.prediction = prediction;
        }

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            this.label = label;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
