package com.example.shop.pricing;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.promo.PromoEngine;
import com.example.shop.promo.PromotionOutcome;
import com.example.shop.util.Money;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PriceCalculator {
    private final TaxCalculator taxCalculator;
    private final ShippingCalculator shippingCalculator;
    private final PromoEngine promoEngine;
    private final Map<String, String> userTiers; // userId -> tier
    private final Map<String, Product> catalog;
    private final Currency baseCurrency;

    public PriceCalculator(TaxCalculator taxCalculator, ShippingCalculator shippingCalculator, PromoEngine promoEngine, Map<String, String> userTiers, Map<String, Product> catalog, Currency baseCurrency) {
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator");
        this.shippingCalculator = Objects.requireNonNull(shippingCalculator, "shippingCalculator");
        this.promoEngine = Objects.requireNonNull(promoEngine, "promoEngine");
        this.userTiers = Objects.requireNonNull(userTiers, "userTiers");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency");
    }

    public PriceBreakdown price(List<CartLine> lines) {
        if (lines.isEmpty()) {
            Money zero = Money.zero(baseCurrency);
            return new PriceBreakdown(new ArrayList<>(), zero, zero, zero, zero, zero);
        }
        Money zero = Money.zero(lines.get(0).unitPrice().currency());
        Money subtotal = zero;
        for (CartLine line : lines) {
            subtotal = subtotal.add(line.subtotal());
        }
        String userId = "anonymous";
        String tier = userTiers.getOrDefault(userId, "standard");
        PromotionOutcome outcome = promoEngine.apply(lines, tier, userId);
        Money discountedSubtotal = subtotal.subtract(outcome.discount());

        Money taxes = zero;
        for (CartLine line : lines) {
            Money lineTax = taxCalculator.taxFor(productFor(line), line.subtotal(), line.discount());
            taxes = taxes.add(lineTax);
        }

        Money shipping = outcome.freeShipping() ? zero : shippingCalculator.shippingFor(lines, discountedSubtotal);
        Money total = discountedSubtotal.add(taxes).add(shipping);
        List<CartLine> newLines = new ArrayList<>(lines);
        return new PriceBreakdown(newLines, subtotal, outcome.discount(), taxes, shipping, total);
    }

    private Product productFor(CartLine line) {
        Product product = catalog.get(line.productId());
        if (product == null) {
            throw new IllegalStateException("Missing product for line " + line.productId());
        }
        return product;
    }
}
