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
        Order order = new Order("o1", List.of(new CartLine("p", 1, new Money(new BigDecimal("1.00"), usd), Money.zero(usd))), new Money(new BigDecimal("1.00"), usd), Money.zero(usd), Money.zero(usd), List.of());
        repo.save(order);
        assertTrue(repo.findById("o1").isPresent());
        assertEquals(1, repo.all().size());
    }
}
