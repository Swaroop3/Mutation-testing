package com.example.shop.cart;

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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {
    private final Currency usd = Currency.getInstance("USD");

    private CartService buildService(InventoryService inv, Map<String, Product> catalog) {
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator price = new PriceCalculator(taxCalculator, ship, promoEngine, Map.of("anonymous", "standard"), catalog, usd);
        return new CartService(inv, price, catalog, 5, 10);
    }

    @Test
    void addUpdateRemoveItem() {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 10);
        CartService service = buildService(inv, Map.of(p.id(), p));

        Cart cart = service.addItem("c1", p.id(), 2);
        assertEquals(1, cart.lines().size());
        assertEquals(new BigDecimal("20.00"), cart.subtotal().raw());

        cart = service.updateQty("c1", p.id(), 3);
        assertEquals(3, cart.lines().get(0).quantity());

        cart = service.removeItem("c1", p.id());
        assertEquals(0, cart.lines().size());
    }

    @Test
    void rejectsUnknownProductOrLimits() {
        InventoryService inv = new InventoryService();
        CartService service = buildService(inv, Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.addItem("c1", "missing", 1));
    }

    @Test
    void enforcesPerItemLimitAndLineLimit() {
        InventoryService inv = new InventoryService();
        Product p1 = new Product("p1", "Prod1", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        Product p2 = new Product("p2", "Prod2", "general", new Money(new BigDecimal("5.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p1.id(), 10);
        inv.upsertStock(p2.id(), 10);
        Map<String, Product> catalog = Map.of(p1.id(), p1, p2.id(), p2);

        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator ship = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator price = new PriceCalculator(taxCalculator, ship, promoEngine, Map.of("anonymous", "standard"), catalog, usd);

        CartService limitedLines = new CartService(inv, price, catalog, 1, 2);
        limitedLines.addItem("c1", p1.id(), 1);
        assertThrows(IllegalStateException.class, () -> limitedLines.addItem("c1", p2.id(), 1), "Should block when line limit reached");
        assertThrows(IllegalArgumentException.class, () -> limitedLines.updateQty("c1", p1.id(), 3), "Should block per-item limit");
    }

    @Test
    void updateFailsWhenItemMissingOrStockLow() {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "general", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 1);
        CartService service = buildService(inv, Map.of(p.id(), p));

        assertThrows(IllegalStateException.class, () -> service.updateQty("c-missing", p.id(), 1));

        service.addItem("c1", p.id(), 1);
        inv.upsertStock(p.id(), 1); // reduce availability so update to 2 will fail
        assertThrows(IllegalStateException.class, () -> service.updateQty("c1", p.id(), 2));
    }

    @Test
    void getCartOnNewIdReturnsEmptyTotals() {
        InventoryService inv = new InventoryService();
        CartService service = buildService(inv, Map.of());
        Cart cart = service.getCart("new");
        assertTrue(cart.lines().isEmpty());
        assertEquals(BigDecimal.ZERO.setScale(2), cart.total().raw());
    }
}
