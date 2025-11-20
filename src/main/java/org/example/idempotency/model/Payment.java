package org.example.idempotency.model;

public record Payment(
        String id,
        String orderId,
        Double amount,
        String currency,
        String status) {
}
