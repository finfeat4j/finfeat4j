package com.github.finfeat4j.api;

import com.github.finfeat4j.api.LabelProducer.Result;
import com.github.finfeat4j.label.Instance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return id == result.id;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    /**
     * This class allows to hold a buffer of instances and once labels are available, it will
     * return labeled instances back. Helpful in online learning.
     */
    class OnlineLabelProducer implements Indicator<Instance, Instance[]> {

        private final LabelProducer producer;
        private final ArrayList<Instance> buffer = new ArrayList<>(50);
        private final long stopOnId;
        private double min;
        private double max;

        private Result reversal;

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
                    var label = this.reversal != null ? this.reversal.label() : this.reverseLabel(reversal.label());
                    while (!this.buffer.isEmpty()
                        && this.buffer.getFirst().timestamp() < reversal.id()) {
                        var removed = this.buffer.removeFirst();
                        removed.setActual(label);
                        removed.setWeight(1.0);
                        instances.add(removed);
                    }
                }
                this.min = reversal.price().doubleValue();
                this.max = reversal.price().doubleValue();
                this.reversal = reversal;
            }
            if (this.reversal != null) {
                Label label = reversal.label();
                int stop = 0;
                if (reversals.length > 0) {
                    var arr = this.buffer.stream().mapToDouble(Instance::trendPrice).toArray();
                    int max = argmax(arr);
                    int min = argmin(arr);
                    this.min = arr[min];
                    this.max = arr[max];
                    stop = label == Label.SELL ? min : max;
                } else {
                    boolean trendContinues = false;
                    if (label == Label.SELL && this.min > instance.trendPrice()) {
                        this.min = instance.trendPrice();
                        trendContinues = true;
                    } else if (label == Label.BUY && this.max < instance.trendPrice()) {
                        this.max = instance.trendPrice();
                        trendContinues = true;
                    }
                    if (trendContinues) {
                        stop = this.buffer.size() - 1;
                    }
                }
                for (int i = 0; i < stop; i++) {
                    var removed = this.buffer.removeFirst();
                    removed.setActual(label);
                    instances.add(removed);
                }
            }
            return instances.toArray(Instance[]::new);
        }

        private int argmin(double[] arr) {
            double min = arr[0];
            int index = 0;
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] < min) {
                    min = arr[i];
                    index = i;
                }
            }
            return index;
        }

        private int argmax(double[] arr) {
            double max = arr[0];
            int index = 0;
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] > max) {
                    max = arr[i];
                    index = i;
                }
            }
            return index;
        }

        /**
         * Basic condition to label the tail of the buffer.
         * Depends on the label producer implementation actually
         */
        protected Instance[] labelTail() {
            for (var i : this.buffer) {
                // this might be not correct and online labels can be different, but what can be done?
                i.setActual(this.reversal.label());
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

        private Label reverseLabel(Label label) {
            if (label == Label.BUY) {
                return Label.SELL;
            } else if (label == Label.SELL) {
                return Label.BUY;
            } else {
                return Label.UNKNOWN;
            }
        }
    }

}
