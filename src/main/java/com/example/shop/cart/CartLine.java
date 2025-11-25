package com.example.shop.cart;

import com.example.shop.util.Money;
import java.util.Objects;

public final class CartLine {
    private final String productId;
    private final int quantity;
    private final Money unitPrice;
    private final Money discount;

    public CartLine(String productId, int quantity, Money unitPrice, Money discount) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.productId = Objects.requireNonNull(productId, "productId");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.discount = Objects.requireNonNull(discount, "discount");
    }

    public String productId() { return productId; }
    public int quantity() { return quantity; }
    public Money unitPrice() { return unitPrice; }
    public Money discount() { return discount; }

    public Money subtotal() {
        return unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
    }
}
