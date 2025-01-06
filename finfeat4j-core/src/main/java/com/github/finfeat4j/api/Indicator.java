package com.github.finfeat4j.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An indicator is a stateful function that takes an input and returns an output.
 * Its implementation should take care about forward only processing.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
public interface Indicator<T, R> extends Function<T, R> {

    /**
     * @return parameters of the indicator
     */
    default Params getParams() {
        return null;
    }

    /**
     * @param attrs the attributes to append to the name
     * @return the final name of the indicator including attrs and params
     */
    default String getName(Object... attrs) {
        var params = getParams();
        var name = this.simpleName();
        if (params != null) {
            name += "(" + params;
        }
        if (attrs != null && attrs.length > 0) {
            if (params != null) {
                name += ",";
            } else {
                name += "(";
            }
            name += Stream.of(attrs)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        }
        if (params != null || (attrs != null && attrs.length > 0)) {
            name += ")";
        }
        return name;
    }

    /**
     * @param rename the new name of the indicator
     * @return a new indicator with the given name
     */
    default Indicator<T, R> rename(String rename) {
        return new Wrapper<>(this, rename) {
            @Override
            public String simpleName() {
                return rename;
            }

            @Override
            public String getName(Object... attrs) {
                return super.getName();
            }
        };
    }

    /**
     * Rounds a double value to a given scale.
     * @param value the value to round
     * @param scale the scale to round to
     * @return the rounded value
     */
    default BigDecimal round(double value, BigDecimal scale) {
        return round(value, scale.scale());
    }

    /**
     * Rounds a double value to a given scale.
     * @param value the value to round
     * @param scale the scale to round to
     * @return the rounded value
     */
    default BigDecimal round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.FLOOR);
    }

    /**
     * @return the simple name of the indicator
     */
    default String simpleName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @param names of the array elements
     * @return Creates an array producer of this indicator.
     */
    default ArrayProducer<T,R> array(String... names) {
        return ArrayProducer.defaultProducer(this, names.length, names);
    }

    default ArrayProducer<T,R> array(int size) {
        return ArrayProducer.defaultProducer(this, size);
    }

    /**
     * Compose this indicator with another one.
     * @param after the indicator to apply after this indicator is applied
     * @return a composed indicator
     * @param <V> the output type of the composed indicator
     */
    default <V> Indicator<T, V> then(Indicator<R, V> after) {
        Function<T, V> func = (T t) -> after.apply(this.apply(t));
        var wrapped = new Wrapper<>(func, after.getName(this.getName()));
        if (after instanceof ArrayProducer<R, V>) {
            return ArrayProducer.defaultProducer(wrapped, ((ArrayProducer<R,V>) after).size());
        }
        return wrapped;
    }

    /**
     * Compose this indicator with another one.
     * @param name the name of the composed indicator
     * @param after the indicator to apply after this indicator is applied
     * @return a composed indicator
     * @param <V> the output type of the composed indicator
     */
    default <V> Indicator<T, V> then(String name, Function<R, V> after, Object... params) {
        return then(new Wrapper<>(after, name, params));
    }

    /**
     * Returns a composed function that first applies this function to its input, and then applies the {@code after}
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} function
     * @param <V> the output type of the {@code after} function
     * todo: remove this method
     */
    @SuppressWarnings("unchecked")
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        if (after instanceof Indicator) {
            return this.then((Indicator<R, V>) after);
        } else {
            return this.then(null, (Function<R, V>) after);
        }
    }


    /**
     * Compose this indicator with another one.
     * @param name the name of the composed indicator
     * @param after the indicator to apply after this indicator is applied
     * @return a composed indicator
     * @param <V> the output type of the composed indicator
     */
    default <V> Indicator<T, V> andThen(String name, Function<R, V> after, Object... params) {
        return this.then(new Wrapper<>(after, name, params));
    }


    /**
     * @param indicator function to wrap
     * @param name of the indicator
     * @param params of the indicator
     * @return a new indicator from a function
     * @param <T> the input type
     * @param <R> the output type
     */
    static <T, R> Indicator<T, R> of(Function<T, R> indicator, String name, Object... params) {
        return new Wrapper<>(indicator, name, params);
    }

    static Indicator<double[], Double> of(int featureIdx, String name) {
        return new Wrapper<>(values -> values[featureIdx], name);
    }

    /**
     * Wraps a function into an indicator.
     * @param <T> the input type
     * @param <R> the output type
     */
    class Wrapper<T, R> implements Indicator<T, R> {

        private final Function<T, R> indicator;
        private final String name;
        private final Params params;

        public Wrapper(Function<T, R> indicator, String name, Object... params) {
            this.name = name;
            this.indicator = indicator;
            this.params = params != null && params.length > 0 ? new Params(params) : null;
        }

        @Override
        public R apply(T t) {
            return indicator.apply(t);
        }

        @Override
        public String simpleName() {
            // note FUNC is not correct, should always have some unique name
            return name != null ? name : "FUNC";
        }

        @Override
        public Params getParams() {
            return this.params;
        }
    }

    record Params(Object... values) {
        @Override
        public String toString() {
            return Stream.of(values)
                .map(o -> {
                    if (o instanceof Number && !(o instanceof Integer)) {
                        return String.format("%.2f", o);
                    }
                    return o.toString();
                })
                .collect(Collectors.joining(","));
        }
    }

    interface ArrayProducer<T, R> extends Indicator<T, R> {
        int size();

        default String[] arrayNames() {
            return null;
        };

        static <A, B> ArrayProducer<A, B> defaultProducer(Indicator<A, B> indicator, int size, String... names) {
            return new ArrayProducer<>() {
                @Override
                public int size() {
                    return size;
                }

                @Override
                public B apply(A a) {
                    return indicator.apply(a);
                }

                @Override
                public String getName(Object... attrs) {
                    return indicator.getName(attrs);
                }

                @Override
                public String[] arrayNames() {
                    return names;
                }
            };
        }

        static ArrayProducer<double[], double[]> defaultReducer(int[] selected) {
            return new ArrayProducer<>() {
                @Override
                public int size() {
                    return selected.length;
                }

                @Override
                public double[] apply(double[] doubles) {
                    var result = new double[selected.length];
                    for (int i = 0; i < selected.length; i++) {
                        result[i] = doubles[selected[i]];
                    }
                    return result;
                }
            };
        }

        static ArrayProducer<double[], double[]> defaultReducer(int[] toDrop, int total) {
            var selected = new int[total - toDrop.length];
            int j = 0;
            for (int i = 0; i < total; i++) {
                if (j < toDrop.length && toDrop[j] == i) {
                    j++;
                } else {
                    selected[i - j] = i;
                }
            }
            return defaultReducer(selected);
        }
    }
}
