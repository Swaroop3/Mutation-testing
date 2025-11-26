package com.example.shop.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    private final Currency usd = Currency.getInstance("USD");
    private final Currency eur = Currency.getInstance("EUR");

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
        Money eurMoney = new Money(new BigDecimal("1.00"), eur);
        assertThrows(IllegalArgumentException.class, () -> usdMoney.add(eurMoney));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.subtract(eurMoney));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.min(eurMoney));
        assertThrows(IllegalArgumentException.class, () -> usdMoney.max(eurMoney));
    }

    @Test
    void compareMinMax() {
        Money low = new Money(new BigDecimal("1.00"), usd);
        Money high = new Money(new BigDecimal("2.00"), usd);
        assertEquals(low, low.min(high));
        assertEquals(high, low.max(high));
        assertEquals(low, low.min(low));
        assertEquals(high, high.max(high));
    }

    @Test
    void multipliesWithRounding() {
        Money a = new Money(new BigDecimal("1.00"), usd);
        Money result = a.multiply(new BigDecimal("1.005"));
        assertEquals(new BigDecimal("1.00"), result.raw());
    }

    @Test
    void equalsAndHashCode() {
        Money a = new Money(new BigDecimal("1.00"), usd);
        Money b = new Money(new BigDecimal("1.00"), usd);
        Money c = new Money(new BigDecimal("2.00"), usd);
        Money d = new Money(new BigDecimal("1.00"), eur);

        assertTrue(a.equals(b));
        assertTrue(a.hashCode() == b.hashCode());

        assertFalse(a.equals(c));
        assertFalse(a.equals(d));
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
    }

    @Test
    void testToString() {
        Money a = new Money(new BigDecimal("1.00"), usd);
        assertEquals("USD 1.00", a.toString());
    }
}
