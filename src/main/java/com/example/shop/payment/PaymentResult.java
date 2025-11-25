package com.example.shop.payment;

import java.util.Objects;

public final class PaymentResult {
    private final PaymentStatus status;
    private final String authCode;
    private final String failureReason;

    private PaymentResult(PaymentStatus status, String authCode, String failureReason) {
        this.status = Objects.requireNonNull(status, "status");
        this.authCode = authCode;
        this.failureReason = failureReason;
    }

    public static PaymentResult authorized(String authCode) {
        return new PaymentResult(PaymentStatus.AUTHORIZED, authCode, null);
    }

    public static PaymentResult declined(String reason) {
        return new PaymentResult(PaymentStatus.DECLINED, null, reason);
    }

    public static PaymentResult error(String reason) {
        return new PaymentResult(PaymentStatus.ERROR, null, reason);
    }

    public PaymentStatus status() { return status; }
    public String authCode() { return authCode; }
    public String failureReason() { return failureReason; }
}
