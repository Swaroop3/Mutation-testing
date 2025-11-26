package com.example.shop.order;

import com.example.shop.cart.CartLine;
import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {
    private final Currency usd = Currency.getInstance("USD");

    @Test
    void savesAndFinds() {
        OrderRepository repo = new OrderRepository();
        var line = new CartLine("p", 1, new Money(new BigDecimal("1.00"), usd), Money.zero(usd));
        var lines = List.of(line);
        var total = new Money(new BigDecimal("1.00"), usd);
        var taxes = new Money(new BigDecimal("0.10"), usd);
        var shipping = new Money(new BigDecimal("5.00"), usd);
        var promotions = List.of("PROMO1");
        Order order = new Order("o1", lines, total, taxes, shipping, promotions);
        repo.save(order);

        var found = repo.findById("o1");
        assertTrue(found.isPresent());
        assertEquals("o1", found.get().id());
        assertEquals(lines, found.get().lines());
        assertEquals(total, found.get().total());
        assertEquals(taxes, found.get().taxes());
        assertEquals(shipping, found.get().shipping());
        assertEquals(promotions, found.get().promotions());

        assertEquals(1, repo.all().size());
    }

    @Test
    void returnsEmptyWhenNotFound() {
        OrderRepository repo = new OrderRepository();
        assertTrue(repo.findById("non-existent-id").isEmpty());
    }
}
