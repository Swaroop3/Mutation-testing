package com.example.shop.pricing;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.promo.CouponValidator;
import com.example.shop.promo.PromoEngine;
import com.example.shop.promo.Promotion;
import com.example.shop.promo.PromotionType;
import com.example.shop.util.FixedClock;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    private final Currency usd = Currency.getInstance("USD");

    @Test
    void calculatesSubtotalDiscountTaxAndShipping() {
        Money unit = new Money(new BigDecimal("10.00"), usd);
        Product product = new Product("sku-1", "Widget", "general", unit, TaxCode.STANDARD, false, false);
        Map<String, Product> catalog = Map.of(product.id(), product);

        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator shippingCalculator = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);

        Promotion promo = Promotion.builder("PROMO10", PromotionType.PERCENTAGE)
                .percent(new BigDecimal("0.10"))
                .build();
        PromoEngine promoEngine = new PromoEngine(List.of(promo), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);

        PriceCalculator priceCalculator = new PriceCalculator(taxCalculator, shippingCalculator, promoEngine, Map.of("anonymous", "standard"), catalog, usd);

        CartLine line = new CartLine(product.id(), 2, unit, Money.zero(usd));
        PriceBreakdown breakdown = priceCalculator.price(List.of(line));

        assertEquals(new BigDecimal("20.00"), breakdown.subtotal().raw());
        assertEquals(new BigDecimal("2.00"), breakdown.discounts().raw());
        assertEquals(new BigDecimal("2.00"), breakdown.taxes().raw());
        assertEquals(new BigDecimal("5.00"), breakdown.shipping().raw());
        assertEquals(new BigDecimal("25.00"), breakdown.total().raw());
    }

    @Test
    void handlesEmptyCart() {
        Map<String, Product> catalog = Map.of();
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(), true);
        ShippingCalculator shippingCalculator = new ShippingCalculator(Money.zero(usd), Money.zero(usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator calculator = new PriceCalculator(taxCalculator, shippingCalculator, promoEngine, Map.of(), catalog, usd);

        PriceBreakdown breakdown = calculator.price(List.of());

        assertEquals(BigDecimal.ZERO.setScale(2), breakdown.total().raw());
    }

    @Test
    void appliesFreeShippingPromotion() {
        Money unit = new Money(new BigDecimal("10.00"), usd);
        Product product = new Product("sku-1", "Widget", "general", unit, TaxCode.STANDARD, false, true);
        Map<String, Product> catalog = Map.of(product.id(), product);

        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator shippingCalculator = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);

        Promotion freeShip = Promotion.builder("FREESHIP", PromotionType.FREE_SHIPPING).freeShipping(true).build();
        PromoEngine promoEngine = new PromoEngine(List.of(freeShip), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);

        PriceCalculator priceCalculator = new PriceCalculator(taxCalculator, shippingCalculator, promoEngine, Map.of("anonymous", "standard"), catalog, usd);
        CartLine line = new CartLine(product.id(), 1, unit, Money.zero(usd));
        PriceBreakdown breakdown = priceCalculator.price(List.of(line));

        assertEquals(BigDecimal.ZERO.setScale(2), breakdown.shipping().raw());
    }
}
