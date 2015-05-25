/*
 * Copyright 2015 h-j-k. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        Map<Denomination, Integer> result = new EnumMap<>(Denomination.class);
        double temp = input;
        for (Denomination current : Denomination.values()) {
            if (current.canBreakdown(temp)) {
                double[] parts = current.breakdown(temp);
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
        return recurse(Stream.of(Denomination.values()).iterator(),
                Denomination.quantize(input), new EnumMap<>(Denomination.class));
    }

    /**
     * @param input the value to wrap
     * @return {@code null} if {@code 0}, else an {@link Integer} instance
     */
    private static Integer getValue(int input) {
        return input == 0 ? null : Integer.valueOf(input);
    }

    private static Map<Denomination, Integer> recurse(Iterator<Denomination> iterator,
            int input, Map<Denomination, Integer> result) {
        if (input == 0 || !iterator.hasNext()) {
            return Collections.unmodifiableMap(result);
        }
        Denomination current = iterator.next();
        return recurse(iterator, result.computeIfAbsent(current,
                    key -> getValue(input / key.getCentValue())) == null ?
                            input : input % current.getCentValue(), result);
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
