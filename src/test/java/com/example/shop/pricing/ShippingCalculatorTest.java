package com.example.shop.pricing;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingCalculatorTest {
    private final Currency usd = Currency.getInstance("USD");

    @Test
    void freeShippingAboveThreshold() {
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("100.00"), usd), TaxCode.STANDARD, false, true);
        CartLine line = new CartLine(p.id(), 1, p.price(), Money.zero(usd));
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("10.00"), usd), new Money(new BigDecimal("50.00"), usd), Map.of(p.id(), p), new BigDecimal("2.00"));
        Money shipping = ship.shippingFor(List.of(line), new Money(new BigDecimal("100.00"), usd));
        assertEquals(BigDecimal.ZERO.setScale(2), shipping.raw());
    }

    @Test
    void addsFragileSurcharge() {
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, true);
        CartLine line = new CartLine(p.id(), 2, p.price(), Money.zero(usd));
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), Map.of(p.id(), p), new BigDecimal("1.50"));
        Money shipping = ship.shippingFor(List.of(line), new Money(new BigDecimal("20.00"), usd));
        assertEquals(new BigDecimal("8.00"), shipping.raw());
    }

    @Test
    void usesBaseWhenNoFragileAndBelowThreshold() {
        Product p = new Product("p", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(p.id(), 1, p.price(), Money.zero(usd));
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), Map.of(p.id(), p), new BigDecimal("1.00"));
        Money shipping = ship.shippingFor(List.of(line), new Money(new BigDecimal("10.00"), usd));
        assertEquals(new BigDecimal("5.00"), shipping.raw());
    }
}
