package com.example.shop.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    private final Currency usd = Currency.getInstance("USD");

    @Test
    void addsAndSubtracts() {
        Money a = new Money(new BigDecimal("10.00"), usd);
        Money b = new Money(new BigDecimal("5.25"), usd);
        assertEquals(new BigDecimal("15.25"), a.add(b).raw());
        assertEquals(new BigDecimal("4.75"), a.subtract(b).raw());
    }

    @Test
    void rejectsCurrencyMismatch() {
        Money usdMoney = new Money(new BigDecimal("1.00"), usd);
        Money eurMoney = new Money(new BigDecimal("1.00"), Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.add(eurMoney));
    }

    @Test
    void compareMinMax() {
        Money low = new Money(new BigDecimal("1.00"), usd);
        Money high = new Money(new BigDecimal("2.00"), usd);
        assertEquals(low, low.min(high));
        assertEquals(high, low.max(high));
    }

    @Test
    void multipliesWithRounding() {
        Money a = new Money(new BigDecimal("1.00"), usd);
        Money result = a.multiply(new BigDecimal("1.005"));
        assertEquals(new BigDecimal("1.00"), result.raw());
    }
}
