package org.example.idempotency.dto;

import org.springframework.http.HttpStatusCode;

public record PaymentResponse(
        String paymentId,
        String orderId,
        Double amount,
        String currency,
        String status,
        HttpStatusCode httpStatus) {
}
