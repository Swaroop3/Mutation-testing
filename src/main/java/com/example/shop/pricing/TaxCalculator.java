package com.example.shop.pricing;

import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.util.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public class TaxCalculator {
    private final Map<TaxCode, BigDecimal> rates;
    private final boolean taxAfterDiscount;

    public TaxCalculator(Map<TaxCode, BigDecimal> rates, boolean taxAfterDiscount) {
        this.rates = Objects.requireNonNull(rates, "rates");
        this.taxAfterDiscount = taxAfterDiscount;
    }

    public Money taxFor(Product product, Money lineAmount, Money lineDiscount) {
        BigDecimal rate = rates.getOrDefault(product.taxCode(), BigDecimal.ZERO);
        Money base = taxAfterDiscount ? lineAmount.subtract(lineDiscount) : lineAmount;
        BigDecimal taxValue = base.raw().multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
        return new Money(taxValue, base.currency());
    }
}
