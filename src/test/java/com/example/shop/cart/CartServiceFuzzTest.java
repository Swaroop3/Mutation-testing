package com.example.shop.cart;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.catalog.InventoryService;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.pricing.PriceCalculator;
import com.example.shop.pricing.ShippingCalculator;
import com.example.shop.pricing.TaxCalculator;
import com.example.shop.promo.CouponValidator;
import com.example.shop.promo.PromoEngine;
import com.example.shop.util.FixedClock;
import com.example.shop.util.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public class CartServiceFuzzTest {

    private final Currency usd = Currency.getInstance("USD");

    private CartService buildService(InventoryService inv, Map<String, Product> catalog) {
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator price = new PriceCalculator(taxCalculator, ship, promoEngine, Map.of("anonymous", "standard"), catalog, usd);
        return new CartService(inv, price, catalog, 5, 10);
    }

    @FuzzTest
    void addItemFuzzTest(FuzzedDataProvider data) {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 10);
        CartService service = buildService(inv, Map.of(p.id(), p));

        String cartId = data.consumeString(10);
        String productId = data.consumeString(10);
        int qty = data.consumeInt();

        try {
            service.addItem(cartId, productId, qty);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Expected exceptions
        }
    }
}
