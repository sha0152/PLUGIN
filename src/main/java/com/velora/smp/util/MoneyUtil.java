package com.velora.smp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    private MoneyUtil() {
    }

    public static BigDecimal parsePositive(String input, int scale) {
        BigDecimal value = parse(input, scale);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return value;
    }

    public static BigDecimal parseNonNegative(String input, int scale) {
        BigDecimal value = parse(input, scale);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        return value;
    }

    public static BigDecimal parse(String input, int scale) {
        BigDecimal value = new BigDecimal(input);
        if (value.scale() > scale) {
            value = value.setScale(scale, RoundingMode.HALF_UP);
        } else {
            value = value.setScale(scale, RoundingMode.HALF_UP);
        }
        return value;
    }

    public static String format(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }
}
