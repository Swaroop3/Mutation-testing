package com.example.shop.payment;

import com.example.shop.util.Money;

public interface PaymentGateway {
    PaymentResult authorize(String orderId, Money amount);
}
