package com.ikueb.breakdown;

import java.math.BigDecimal;
import java.util.Objects;

public enum Denomination {
    A_MILLION(1_000_000, "$1 million"),
    FIFTY_DOLLARS(50, "$50"),
    TWENTY_DOLLARS(20, "$20"),
    TEN_DOLLARS(10, "$10"),
    FIVE_DOLLARS(5, "$5"),
    DOLLAR_NINETY_NINE(1.99, "$1.99"),
    A_DOLLAR(1, "$1"),
    QUARTER(0.25, "25¢"),
    DIME(0.1, "10¢"),
    NICKEL(0.05, "5¢"),
    A_CENT(0.01, "1¢");

    private final BigDecimal value;
    private String description;

    private Denomination(double value, final String description) {
        this.value = BigDecimal.valueOf(value);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * @param input the value to compare against
     * @return <code>true</code> if <code>input</code> is not smaller than the current value
     */
    public boolean canBreakdown(double input) {
        return BigDecimal.valueOf(input).compareTo(value) >= 0;
    }

    /**
     * Breaks down the input against the current value.
     *
     * @param input the input to start
     * @return a two-element array, the first being the quotient (aka multiplier) and the second
     *         being the remainder
     */
    public double[] breakdown(double input) {
        final BigDecimal[] results = BigDecimal.valueOf(input).divideAndRemainder(value);
        return new double[] { results[0].doubleValue(), results[1].doubleValue() };
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * @param multiplier the value to represent
     * @return a representation of the multiplier and the current value
     */
    public String toString(int multiplier) {
        return String.format("%d x %s", Integer.valueOf(multiplier), toString());
    }

    /**
     * @param multiplier the value to multiply with
     * @return the product of the multiplier and the current value
     */
    public double multiply(int multiplier) {
        return value.multiply(BigDecimal.valueOf(multiplier)).doubleValue();
    }

}
