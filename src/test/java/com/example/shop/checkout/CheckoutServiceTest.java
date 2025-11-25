package com.example.shop.checkout;

import com.example.shop.cart.CartService;
import com.example.shop.catalog.InventoryService;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.order.OrderRepository;
import com.example.shop.payment.PaymentGateway;
import com.example.shop.payment.PaymentResult;
import com.example.shop.payment.PaymentStatus;
import com.example.shop.pricing.PriceCalculator;
import com.example.shop.pricing.ShippingCalculator;
import com.example.shop.pricing.TaxCalculator;
import com.example.shop.promo.CouponValidator;
import com.example.shop.promo.PromoEngine;
import com.example.shop.user.Address;
import com.example.shop.user.User;
import com.example.shop.util.FixedClock;
import com.example.shop.util.IdGenerator;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutServiceTest {
    private final Currency usd = Currency.getInstance("USD");

    private CartService cartService(InventoryService inv, Map<String, Product> catalog) {
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator shipping = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator price = new PriceCalculator(taxCalculator, shipping, promoEngine, Map.of("anonymous", "standard"), catalog, usd);
        return new CartService(inv, price, catalog, 10, 10);
    }

    @Test
    void completesCheckoutOnPaymentSuccess() {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 5);
        Map<String, Product> catalog = Map.of(p.id(), p);
        CartService cartService = cartService(inv, catalog);
        cartService.addItem("c1", p.id(), 1);

        PaymentGateway gateway = (orderId, amount) -> PaymentResult.authorized("OK" + orderId);
        OrderRepository orders = new OrderRepository();
        IdGenerator ids = () -> "order-1";
        CheckoutService checkout = new CheckoutService(cartService, inv, gateway, orders, ids, catalog);

        CheckoutRequest request = new CheckoutRequest("c1", new User("u1", "standard", java.util.Locale.US), new Address("addr", "city", "state", "US", "00000"));
        CheckoutResult result = checkout.checkout(request);

        assertEquals(PaymentStatus.AUTHORIZED, result.paymentResult().status());
        assertTrue(orders.findById("order-1").isPresent());
        assertEquals(4, inv.available(p.id()));
    }

    @Test
    void releasesInventoryOnPaymentFailure() {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 2);
        Map<String, Product> catalog = Map.of(p.id(), p);
        CartService cartService = cartService(inv, catalog);
        cartService.addItem("c2", p.id(), 2);

        PaymentGateway gateway = (orderId, amount) -> PaymentResult.declined("nope");
        OrderRepository orders = new OrderRepository();
        IdGenerator ids = () -> "order-2";
        CheckoutService checkout = new CheckoutService(cartService, inv, gateway, orders, ids, catalog);

        CheckoutRequest request = new CheckoutRequest("c2", new User("u2", "standard", java.util.Locale.US), new Address("addr", "city", "state", "US", "00000"));
        CheckoutResult result = checkout.checkout(request);

        assertEquals(PaymentStatus.DECLINED, result.paymentResult().status());
        assertTrue(orders.all().isEmpty());
        assertEquals(2, inv.available(p.id()));
    }

    @Test
    void throwsWhenInventoryCannotBeReserved() {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 1);
        Map<String, Product> catalog = Map.of(p.id(), p);
        CartService cartService = cartService(inv, catalog);
        cartService.addItem("c3", p.id(), 1);
        inv.upsertStock(p.id(), 0); // simulate stock depletion before checkout

        PaymentGateway gateway = (orderId, amount) -> PaymentResult.authorized("OK" + orderId);
        OrderRepository orders = new OrderRepository();
        IdGenerator ids = () -> "order-3";
        CheckoutService checkout = new CheckoutService(cartService, inv, gateway, orders, ids, catalog);

        CheckoutRequest request = new CheckoutRequest("c3", new User("u3", "standard", java.util.Locale.US), new Address("addr", "city", "state", "US", "00000"));
        assertThrows(IllegalStateException.class, () -> checkout.checkout(request));
        assertTrue(orders.all().isEmpty());
    }
}
