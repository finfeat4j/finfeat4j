package com.github.finfeat4j.core;

import com.github.finfeat4j.util.ArrayProducer;
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

    default Indicator<T,R> array(int size) {
        return new ArrayProducer<>(this, size);
    }

    /**
     * Compose this indicator with another one.
     * @param after the indicator to apply after this indicator is applied
     * @return a composed indicator
     * @param <V> the output type of the composed indicator
     */
    default <V> Indicator<T, V> then(Indicator<R, V> after) {
        Function<T, V> func = (T t) -> after.apply(this.apply(t));
        return new Wrapper<>(func, after.getName(this.getName()));
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

    static <T, R> Indicator<T, R> of(Function<T, R> indicator, String name, Object... params) {
        return new Wrapper<>(indicator, name, params);
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
}
