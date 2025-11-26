package com.example.shop.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PaymentResultTest {

    @Test
    void testAuthorized() {
        // given
        String authCode = "AUTH123";

        // when
        PaymentResult result = PaymentResult.authorized(authCode);

        // then
        assertEquals(PaymentStatus.AUTHORIZED, result.status());
        assertEquals(authCode, result.authCode());
        assertNull(result.failureReason());
    }

    @Test
    void testDeclined() {
        // given
        String reason = "Insufficient funds";

        // when
        PaymentResult result = PaymentResult.declined(reason);

        // then
        assertEquals(PaymentStatus.DECLINED, result.status());
        assertNull(result.authCode());
        assertEquals(reason, result.failureReason());
    }

    @Test
    void testError() {
        // given
        String reason = "Gateway timeout";

        // when
        PaymentResult result = PaymentResult.error(reason);

        // then
        assertEquals(PaymentStatus.ERROR, result.status());
        assertNull(result.authCode());
        assertEquals(reason, result.failureReason());
    }
}
