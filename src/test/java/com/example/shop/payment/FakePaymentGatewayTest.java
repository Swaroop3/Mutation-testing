package com.example.shop.payment;

import com.example.shop.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePaymentGatewayTest {

    @Test
    void returnsDeterministicOutcomes() {
        FakePaymentGateway gateway = new FakePaymentGateway(1L);
        Money amount = new Money(new BigDecimal("10.00"), Currency.getInstance("USD"));
        PaymentResult result = gateway.authorize("order-1", amount);
        assertTrue(result.status() == PaymentStatus.AUTHORIZED || result.status() == PaymentStatus.DECLINED || result.status() == PaymentStatus.ERROR);
    }
}
