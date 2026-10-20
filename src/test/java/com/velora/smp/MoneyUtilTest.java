package com.velora.smp;

import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MoneyUtilTest {

    @Test
    void parseRoundsToScale() {
        BigDecimal value = MoneyUtil.parse("10.239", 2);
        Assertions.assertEquals("10.24", value.toPlainString());
    }

    @Test
    void positiveValidationRejectsZero() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MoneyUtil.parsePositive("0", 2));
    }
}
