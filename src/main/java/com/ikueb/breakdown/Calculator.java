package com.ikueb.breakdown;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for manipulating with {@link Denomination} enums.
 */
public class Calculator {

    private Calculator() {
        // empty
    }

    /**
     * Break down the input into {@link Denomination} values.
     *
     * @param input the value to break down
     * @return an unmodifiable {@link Map} with the {@link Denomination} as keys and a positive
     *         integer, the multiplier, as values
     */
    public static Map<Denomination, Integer> getBreakdown(double input) {
        final Map<Denomination, Integer> result = new EnumMap<>(Denomination.class);
        double temp = input;
        for (final Denomination current : Denomination.values()) {
            if (current.canBreakdown(temp)) {
                final double[] parts = current.breakdown(temp);
                result.put(current, Integer.valueOf(Double.valueOf(parts[0]).intValue()));
                temp = parts[1];
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Break down the input into {@link Denomination} values by recursion.
     *
     * @param input the value to break down
     * @return an unmodifiable {@link Map} with the {@link Denomination} as keys and a
     *         positive integer, the multiplier, as values
     */
    public static Map<Denomination, Integer> getBreakdownRecursively(double input) {
        return Collections.unmodifiableMap(
                recurse(Stream.of(Denomination.values()).iterator(), 
                        (int) (input * Denomination.MULTIPLIER), 
                        new EnumMap<>(Denomination.class)));
    }

    private static Map<Denomination, Integer> recurse(Iterator<Denomination> iterator,
            int input, Map<Denomination, Integer> result) {
        if (input == 0 || !iterator.hasNext()) {
            return result;
        }
        Denomination current = iterator.next();
        int nextInput = input;
        int units = input / current.getCentValue();
        if (units != 0) {
            result.put(current, Integer.valueOf(units));
            nextInput = input % current.getCentValue();
        }
        return recurse(iterator, nextInput, result);
    }

    /**
     * @param map the {@link Map} to generate from
     * @return a human-reable output
     */
    public static String format(Map<Denomination, Integer> map) {
        return Objects.requireNonNull(map).entrySet().stream()
                .map(e -> e.getKey().toString(e.getValue().intValue()))
                .collect(Collectors.joining(", "));
    }

    /**
     * @param map the {@link Map} to generate from
     * @return the sum of the product of the map's keys and values
     */
    public static double compute(Map<Denomination, Integer> map) {
        return Objects.requireNonNull(map).entrySet().stream()
                .mapToDouble(e -> e.getKey().multiply(e.getValue().intValue())).sum();
    }
}
