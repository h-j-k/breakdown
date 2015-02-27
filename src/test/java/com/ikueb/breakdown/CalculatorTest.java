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
        final Map<Denomination, Integer> expected = Objects.requireNonNull(builder).getExpected();
        assertThat(Calculator.getBreakdown(Objects.requireNonNull(testValue).doubleValue()),
                equalTo(expected));
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
         * Let <em>v</em> be the sum of the current value and <code>multiplier</code>.<br>
         * If <em>v</em> is greater than zero, the value is updated as such, else the entry for
         * <code>denominator</code> is removed.<br>
         * As such, the generated {@link Map} will only have denominators with positive multipliers.
         *
         * @param denomination the denomination to add
         * @param multiplier the multiplier to add
         * @return this {@link CaseBuilder}
         */
        CaseBuilder with(final Denomination denomination, int multiplier) {
            final int current = map.getOrDefault(Objects.requireNonNull(denomination),
                    Integer.valueOf(0)).intValue();
            if (current + multiplier > 0) {
                map.put(denomination, Integer.valueOf(current + multiplier));
            } else {
                map.remove(denomination);
            }
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
