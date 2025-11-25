package com.example.shop.cart;

import com.example.shop.util.Money;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Cart {
    private final String id;
    private final List<CartLine> lines;
    private final Money subtotal;
    private final Money discounts;
    private final Money taxes;
    private final Money shipping;
    private final Money total;

    public Cart(String id, List<CartLine> lines, Money subtotal, Money discounts, Money taxes, Money shipping, Money total) {
        this.id = Objects.requireNonNull(id, "id");
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal");
        this.discounts = Objects.requireNonNull(discounts, "discounts");
        this.taxes = Objects.requireNonNull(taxes, "taxes");
        this.shipping = Objects.requireNonNull(shipping, "shipping");
        this.total = Objects.requireNonNull(total, "total");
    }

    public String id() { return id; }
    public List<CartLine> lines() { return lines; }
    public Money subtotal() { return subtotal; }
    public Money discounts() { return discounts; }
    public Money taxes() { return taxes; }
    public Money shipping() { return shipping; }
    public Money total() { return total; }
}
