package com.example.shop.pricing;

import com.example.shop.cart.CartLine;
import com.example.shop.util.Money;
import java.util.Collections;
import java.util.List;

public final class PriceBreakdown {
    private final List<CartLine> lines;
    private final Money subtotal;
    private final Money discounts;
    private final Money taxes;
    private final Money shipping;
    private final Money total;

    public PriceBreakdown(List<CartLine> lines, Money subtotal, Money discounts, Money taxes, Money shipping, Money total) {
        this.lines = Collections.unmodifiableList(lines);
        this.subtotal = subtotal;
        this.discounts = discounts;
        this.taxes = taxes;
        this.shipping = shipping;
        this.total = total;
    }

    public List<CartLine> lines() { return lines; }
    public Money subtotal() { return subtotal; }
    public Money discounts() { return discounts; }
    public Money taxes() { return taxes; }
    public Money shipping() { return shipping; }
    public Money total() { return total; }
}
