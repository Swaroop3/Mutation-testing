package com.example.shop.order;

import com.example.shop.cart.CartLine;
import com.example.shop.util.Money;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Order {
    private final String id;
    private final List<CartLine> lines;
    private final Money total;
    private final Money taxes;
    private final Money shipping;
    private final List<String> promotions;

    public Order(String id, List<CartLine> lines, Money total, Money taxes, Money shipping, List<String> promotions) {
        this.id = Objects.requireNonNull(id, "id");
        this.lines = Collections.unmodifiableList(List.copyOf(lines));
        this.total = Objects.requireNonNull(total, "total");
        this.taxes = Objects.requireNonNull(taxes, "taxes");
        this.shipping = Objects.requireNonNull(shipping, "shipping");
        this.promotions = Collections.unmodifiableList(List.copyOf(promotions));
    }

    public String id() { return id; }
    public List<CartLine> lines() { return lines; }
    public Money total() { return total; }
    public Money taxes() { return taxes; }
    public Money shipping() { return shipping; }
    public List<String> promotions() { return promotions; }
}
