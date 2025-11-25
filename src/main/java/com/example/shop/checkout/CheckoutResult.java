package com.example.shop.checkout;

import com.example.shop.order.Order;
import com.example.shop.payment.PaymentResult;
import java.util.Objects;

public final class CheckoutResult {
    private final Order order;
    private final PaymentResult paymentResult;

    public CheckoutResult(Order order, PaymentResult paymentResult) {
        this.order = Objects.requireNonNull(order, "order");
        this.paymentResult = Objects.requireNonNull(paymentResult, "paymentResult");
    }

    public Order order() { return order; }
    public PaymentResult paymentResult() { return paymentResult; }
}
