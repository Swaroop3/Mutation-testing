package com.example.shop.promo;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.util.FixedClock;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PromoEngineTest {

    private final Currency usd = Currency.getInstance("USD");

    @Test
    void appliesPercentageAndStopsOnExclusive() {
        Product prod = new Product("sku-1", "Widget", "general", new Money(new BigDecimal("20.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));

        Promotion tenOff = Promotion.builder("P10", PromotionType.PERCENTAGE).percent(new BigDecimal("0.10")).combinable(false).build();
        Promotion extra = Promotion.builder("EXTRA", PromotionType.FIXED_AMOUNT).amount(new Money(new BigDecimal("5.00"), usd)).build();

        PromoEngine engine = new PromoEngine(List.of(tenOff, extra), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);
        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");

        assertEquals(new BigDecimal("2.00"), outcome.discount().raw());
        assertEquals(List.of("P10"), outcome.appliedCodes());
    }

    @Test
    void appliesCategoryAndBogoAndFreeShipping() {
        Product catA = new Product("a", "A", "catA", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        Product catB = new Product("b", "B", "catB", new Money(new BigDecimal("8.00"), usd), TaxCode.STANDARD, false, false);
        CartLine lineA = new CartLine(catA.id(), 2, catA.price(), Money.zero(usd));
        CartLine lineB = new CartLine(catB.id(), 1, catB.price(), Money.zero(usd));

        Promotion category = Promotion.builder("CAT20", PromotionType.CATEGORY_PERCENTAGE).category("catA").percent(new BigDecimal("0.20")).build();
        Promotion bogo = Promotion.builder("BOGO", PromotionType.BOGO).category("catA").build();
        Promotion freeShip = Promotion.builder("SHIP", PromotionType.FREE_SHIPPING).freeShipping(true).build();

        PromoEngine engine = new PromoEngine(List.of(category, bogo, freeShip), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(catA.id(), catA, catB.id(), catB), new CouponValidator(), usd);
        PromotionOutcome outcome = engine.apply(List.of(lineA, lineB), "standard", "u1");

        assertTrue(outcome.freeShipping());
        assertEquals(3, outcome.appliedCodes().size());
        assertTrue(outcome.discount().raw().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void respectsTierRestrictions() {
        Product prod = new Product("sku-1", "Widget", "general", new Money(new BigDecimal("20.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion vipOnly = Promotion.builder("VIP", PromotionType.FIXED_AMOUNT).amount(new Money(new BigDecimal("5.00"), usd)).tiersAllowed(Set.of("vip")).build();
        PromoEngine engine = new PromoEngine(List.of(vipOnly), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }

    @Test
    void skipsExpiredPromotions() {
        Product prod = new Product("sku-1", "Widget", "general", new Money(new BigDecimal("20.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion expired = Promotion.builder("OLD", PromotionType.FIXED_AMOUNT)
                .amount(new Money(new BigDecimal("5.00"), usd))
                .endsAt(Instant.parse("2024-12-31T23:59:59Z"))
                .build();
        PromoEngine engine = new PromoEngine(List.of(expired), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
        assertTrue(outcome.appliedCodes().isEmpty());
    }

    @Test
    void enforcesMaxUsagePerUser() {
        Product prod = new Product("sku-1", "Widget", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion limited = Promotion.builder("ONCE", PromotionType.FIXED_AMOUNT)
                .amount(new Money(new BigDecimal("2.00"), usd))
                .maxUsesPerUser(1)
                .build();
        CouponValidator validator = new CouponValidator();
        PromoEngine engine = new PromoEngine(List.of(limited), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), validator, usd);

        PromotionOutcome first = engine.apply(List.of(line), "standard", "user1");
        PromotionOutcome second = engine.apply(List.of(line), "standard", "user1");

        assertEquals(new BigDecimal("2.00"), first.discount().raw());
        assertEquals(BigDecimal.ZERO.setScale(2), second.discount().raw());
    }

    @Test
    void computesBogoDiscountUsingCheapestEligible() {
        Product expensive = new Product("a", "A", "catA", new Money(new BigDecimal("15.00"), usd), TaxCode.STANDARD, false, false);
        Product cheap = new Product("b", "B", "catA", new Money(new BigDecimal("5.00"), usd), TaxCode.STANDARD, false, false);
        CartLine lineExpensive = new CartLine(expensive.id(), 1, expensive.price(), Money.zero(usd));
        CartLine lineCheap = new CartLine(cheap.id(), 1, cheap.price(), Money.zero(usd));
        Promotion bogo = Promotion.builder("BOGO", PromotionType.BOGO).category("catA").build();
        PromoEngine engine = new PromoEngine(List.of(bogo), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(expensive.id(), expensive, cheap.id(), cheap), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(lineExpensive, lineCheap), "standard", "u1");
        assertEquals(new BigDecimal("5.00"), outcome.discount().raw(), "Cheapest item should be discounted in BOGO");
    }

    @Test
    void zeroPercentPromoProducesNoDiscount() {
        Product prod = new Product("sku-1", "Widget", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion zero = Promotion.builder("ZERO", PromotionType.PERCENTAGE).percent(BigDecimal.ZERO).build();
        PromoEngine engine = new PromoEngine(List.of(zero), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }

    @Test
    void testApplyWithEmptyCart() {
        PromoEngine engine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(), new CouponValidator(), usd);
        PromotionOutcome outcome = engine.apply(List.of(), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }

    @Test
    void testBogoWithInsufficientQuantity() {
        Product prod = new Product("a", "A", "catA", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion bogo = Promotion.builder("BOGO", PromotionType.BOGO).category("catA").build();
        PromoEngine engine = new PromoEngine(List.of(bogo), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }

    @Test
    void testCategoryPercentWithNoMatchingItems() {
        Product prod = new Product("a", "A", "catA", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        CartLine line = new CartLine(prod.id(), 1, prod.price(), Money.zero(usd));
        Promotion category = Promotion.builder("CAT20", PromotionType.CATEGORY_PERCENTAGE).category("catB").percent(new BigDecimal("0.20")).build();
        PromoEngine engine = new PromoEngine(List.of(category), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(prod.id(), prod), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }

    @Test
    void testApplyWithProductNotInCatalog() {
        CartLine line = new CartLine("sku-not-in-catalog", 1, new Money(new BigDecimal("10.00"), usd), Money.zero(usd));
        Promotion bogo = Promotion.builder("BOGO", PromotionType.BOGO).category("catA").build();
        PromoEngine engine = new PromoEngine(List.of(bogo), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), Map.of(), new CouponValidator(), usd);

        PromotionOutcome outcome = engine.apply(List.of(line), "standard", "u1");
        assertEquals(BigDecimal.ZERO.setScale(2), outcome.discount().raw());
    }
}
