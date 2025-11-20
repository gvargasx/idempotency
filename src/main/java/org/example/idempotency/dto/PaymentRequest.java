package org.example.idempotency.dto;

public record PaymentRequest(
        String orderId,
        Double amount,
        String currency) {
}
