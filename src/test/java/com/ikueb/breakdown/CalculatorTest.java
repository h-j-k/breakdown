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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.stream.Stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CalculatorTest {

    @DataProvider(name = "test-cases")
    public Iterator<Object[]> getTestCases() {
        final Collection<Object[]> cases = new ArrayList<>();
        addCase(cases, 0, CaseBuilder.createEmpty());
        addCase(cases, 0.01, CaseBuilder.create(Denomination.A_CENT, 1));
        addCase(cases, 1.28, CaseBuilder.create(Denomination.A_DOLLAR, 1)
                                            .with(Denomination.QUARTER, 1)
                                            .with(Denomination.A_CENT, 3));
        addCase(cases, 19.48, CaseBuilder.create(Denomination.TEN_DOLLARS, 1)
                                            .with(Denomination.FIVE_DOLLARS, 1)
                                            .with(Denomination.DOLLAR_NINETY_NINE, 2)
                                            .with(Denomination.QUARTER, 2));
        addCase(cases, 100.75, CaseBuilder.create(Denomination.FIFTY_DOLLARS, 2)
                                            .with(Denomination.QUARTER, 3));
        addCase(cases, 1_000_040.15, CaseBuilder.create(Denomination.A_MILLION, 1)
                                            .with(Denomination.TWENTY_DOLLARS, 2)
                                            .with(Denomination.DIME, 1)
                                            .with(Denomination.NICKEL, 1));
        return cases.iterator();
    }

    @Test(dataProvider = "test-cases")
    public void test(final Double testValue, final CaseBuilder builder) {
        doTest(testValue, builder, Calculator::getBreakdown);
    }

    @Test(dataProvider = "test-cases")
    public void testRecursion(final Double testValue, final CaseBuilder builder) {
        doTest(testValue, builder, Calculator::getBreakdownRecursively);
    }

    private void doTest(final Double testValue, final CaseBuilder builder,
            final DoubleFunction<Map<Denomination, Integer>> function) {
        Stream.of(testValue, builder).forEach(Objects::requireNonNull);
        final Map<Denomination, Integer> expected = builder.getExpected();
        assertThat(function.apply(testValue.doubleValue()), equalTo(expected));
        assertThat(Double.valueOf(Calculator.compute(expected)), equalTo(testValue));
    }

    private static void addCase(final Collection<Object[]> cases, double testValue,
            final CaseBuilder builder) {
        Objects.requireNonNull(cases).add(new Object[] { Double.valueOf(testValue), builder });
    }

    /**
     * Helper class to build the expected {@link Map} of denominations and multipliers.
     */
    private static final class CaseBuilder {
        private final Map<Denomination, Integer> map;

        private CaseBuilder(final Map<Denomination, Integer> map) {
            this.map = map;
        }

        static CaseBuilder create(final Denomination denomination, int multiplier) {
            return new CaseBuilder(new EnumMap<>(Denomination.class))
                    .with(denomination, multiplier);
        }

        static CaseBuilder createEmpty() {
            return new CaseBuilder(Collections.EMPTY_MAP);
        }

        /**
         * Let <em>v</em> be the sum of the current value and {@code multiplier}.<br>
         * If <em>v</em> is greater than zero, the value is updated as such, else the
         * entry for {@code denomination} is removed.<br>
         * As such, the generated {@link Map} will only have denominators with positive
         * multipliers, and any negative multiplers at any stage will remove the
         * {@code denomination} mapping.
         *
         * @param denomination the denomination to add
         * @param multiplier the multiplier to add
         * @return this {@link CaseBuilder}
         */
        CaseBuilder with(final Denomination denomination, int multiplier) {
            map.merge(denomination, Integer.valueOf(multiplier), (now, in) -> {
                int total = now.intValue() + in.intValue();
                return total > 0 ? Integer.valueOf(total) : null; });
            return this;
        }

        /**
         * @return an unmodifiable copy of the underlying {@link Map}
         */
        Map<Denomination, Integer> getExpected() {
            return Collections.unmodifiableMap(map);
        }

        /**
         * @return a human-reable output
         * @see Calculator#format(Map)
         */
        @Override
        public String toString() {
            return Calculator.format(map);
        }
    }

}
