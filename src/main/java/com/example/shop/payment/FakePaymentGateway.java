package com.example.shop.payment;

import com.example.shop.util.Money;
import java.util.Random;

public class FakePaymentGateway implements PaymentGateway {
    private final Random random;

    public FakePaymentGateway(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public PaymentResult authorize(String orderId, Money amount) {
        int r = random.nextInt(100);
        if (r < 80) {
            return PaymentResult.authorized("AUTH-" + orderId);
        } else if (r < 90) {
            return PaymentResult.declined("Issuer declined");
        } else {
            return PaymentResult.error("Gateway timeout");
        }
    }
}
