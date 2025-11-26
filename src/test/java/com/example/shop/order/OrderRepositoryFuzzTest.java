package com.example.shop.order;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.cart.CartLine;
import com.example.shop.util.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrderRepositoryFuzzTest {

    private final Currency usd = Currency.getInstance("USD");

    private List<CartLine> generateFuzzedCartLines(FuzzedDataProvider data) {
        int numLines = data.consumeInt(1, 5);
        return IntStream.range(0, numLines)
                .mapToObj(i -> {
                    String productId = data.consumeString(10);
                    int quantity = data.consumeInt(1, 100);
                    double priceDouble = data.consumeDouble();
                    BigDecimal price = Double.isFinite(priceDouble) ? BigDecimal.valueOf(priceDouble) : BigDecimal.ZERO;
                    return new CartLine(productId, quantity, new Money(price, usd), Money.zero(usd));
                })
                .collect(Collectors.toList());
    }

    @FuzzTest
    void saveOrderFuzzTest(FuzzedDataProvider data) {
        OrderRepository repo = new OrderRepository();
        List<CartLine> lines = generateFuzzedCartLines(data);
        double totalDouble = data.consumeDouble();
        Money total = new Money(Double.isFinite(totalDouble) ? BigDecimal.valueOf(totalDouble) : BigDecimal.ZERO, usd);
        double taxesDouble = data.consumeDouble();
        Money taxes = new Money(Double.isFinite(taxesDouble) ? BigDecimal.valueOf(taxesDouble) : BigDecimal.ZERO, usd);
        double shippingDouble = data.consumeDouble();
        Money shipping = new Money(Double.isFinite(shippingDouble) ? BigDecimal.valueOf(shippingDouble) : BigDecimal.ZERO, usd);

        List<String> promotions = IntStream.range(0, data.consumeInt(0, 3))
                .mapToObj(i -> data.consumeString(10))
                .collect(Collectors.toList());

        Order order = new Order(data.consumeString(20), lines, total, taxes, shipping, promotions);

        try {
            repo.save(order);
            repo.findById(order.id());
            repo.all();
        } catch (Exception e) {
            // Catching any potential exceptions
        }
    }
}
