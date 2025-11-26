package com.example.shop.promo;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.cart.Cart;
import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.util.FixedClock;
import com.example.shop.util.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public class PromoEngineFuzzTest {

    private final Currency usd = Currency.getInstance("USD");

    @FuzzTest
    void applyFuzzTest(FuzzedDataProvider data) {
        Product p1 = new Product("p1", "Prod1", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        Map<String, Product> catalog = Map.of("p1", p1);
        CouponValidator validator = new CouponValidator();
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, validator, usd);

        int quantity = data.consumeInt(1, 10);
        CartLine line = new CartLine("p1", quantity, p1.price(), Money.zero(usd));
        Money subtotal = p1.price().multiply(new BigDecimal(quantity));
        Cart cart = new Cart("c1", List.of(line), subtotal, Money.zero(usd), Money.zero(usd), Money.zero(usd), subtotal);

        String promoCode = data.consumeString(20);
        String userTier = data.consumeString(10);
        String userId = data.consumeString(10);

        try {
            promoEngine.apply(cart.lines(), userTier, userId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Expected exceptions
        }
    }
}
