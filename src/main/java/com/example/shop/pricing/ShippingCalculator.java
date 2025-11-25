package com.example.shop.pricing;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.util.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShippingCalculator {
    private final Money base;
    private final Money freeThreshold;
    private final Map<String, Product> catalog;
    private final BigDecimal fragileSurcharge;

    public ShippingCalculator(Money base, Money freeThreshold, Map<String, Product> catalog, BigDecimal fragileSurcharge) {
        this.base = Objects.requireNonNull(base, "base");
        this.freeThreshold = Objects.requireNonNull(freeThreshold, "freeThreshold");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.fragileSurcharge = fragileSurcharge;
    }

    public Money shippingFor(List<CartLine> lines, Money subtotal) {
        if (subtotal.raw().compareTo(freeThreshold.raw()) >= 0) {
            return Money.zero(base.currency());
        }
        BigDecimal amount = base.raw();
        for (CartLine line : lines) {
            Product product = catalog.get(line.productId());
            if (product != null && product.isFragile()) {
                BigDecimal surcharge = fragileSurcharge.multiply(BigDecimal.valueOf(line.quantity()));
                amount = amount.add(surcharge);
            }
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        return new Money(amount, base.currency());
    }
}
