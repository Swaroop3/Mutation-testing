package com.example.shop.checkout;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.cart.CartService;
import com.example.shop.catalog.InventoryService;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.order.OrderRepository;
import com.example.shop.payment.PaymentGateway;
import com.example.shop.payment.PaymentResult;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutServiceFuzzTest {

    private final Currency usd = Currency.getInstance("USD");

    private CartService cartService(InventoryService inv, Map<String, Product> catalog) {
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator shipping = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator price = new PriceCalculator(taxCalculator, shipping, promoEngine, Map.of("anonymous", "standard"), catalog, usd);
        return new CartService(inv, price, catalog, 10, 10);
    }

    @FuzzTest
    void checkoutFuzzTest(FuzzedDataProvider data) {
        InventoryService inv = new InventoryService();
        Product p = new Product("p1", "Prod", "cat", new Money(new BigDecimal("10.00"), usd), TaxCode.STANDARD, false, false);
        inv.upsertStock(p.id(), 5);
        Map<String, Product> catalog = Map.of(p.id(), p);
        CartService cartService = cartService(inv, catalog);
        String cartId = "c1";
        cartService.addItem(cartId, p.id(), 1);

        PaymentGateway gateway = (orderId, amount) -> PaymentResult.authorized("OK" + orderId);
        OrderRepository orders = new OrderRepository();
        IdGenerator ids = () -> "order-1";
        CheckoutService checkout = new CheckoutService(cartService, inv, gateway, orders, ids, catalog);

        User user = new User(data.consumeString(10), data.consumeString(10), Locale.US);
        Address address = new Address(data.consumeString(20), data.consumeString(10), data.consumeString(10), data.consumeString(2), data.consumeString(5));
        CheckoutRequest request = new CheckoutRequest(data.pickValue(new String[]{cartId, data.consumeString(10)}), user, address);

        try {
            checkout.checkout(request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Expected exceptions
        }
    }
}
