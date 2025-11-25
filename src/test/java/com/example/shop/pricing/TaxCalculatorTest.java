package com.example.shop.pricing;

import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculatorTest {
    private final Currency usd = Currency.getInstance("USD");

    @Test
    void taxesAfterDiscount() {
        TaxCalculator calc = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        Money tax = calc.taxFor(p, new Money(new BigDecimal("20.00"), usd), new Money(new BigDecimal("5.00"), usd));
        assertEquals(new BigDecimal("1.50"), tax.raw());
    }

    @Test
    void taxesBeforeDiscountWhenConfigured() {
        TaxCalculator calc = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), false);
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        Money tax = calc.taxFor(p, new Money(new BigDecimal("20.00"), usd), new Money(new BigDecimal("5.00"), usd));
        assertEquals(new BigDecimal("2.00"), tax.raw());
    }

    @Test
    void zeroRateProducesZeroTax() {
        TaxCalculator calc = new TaxCalculator(Map.of(TaxCode.EXEMPT, BigDecimal.ZERO), true);
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.EXEMPT, false, false);
        Money tax = calc.taxFor(p, new Money(new BigDecimal("20.00"), usd), Money.zero(usd));
        assertEquals(BigDecimal.ZERO.setScale(2), tax.raw());
    }
}
