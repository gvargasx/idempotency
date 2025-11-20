package org.example.idempotency.model;

import org.springframework.http.HttpStatusCode;

public record IdempotencyEntry(
        HttpStatusCode status,
        String responseBody
) {
}



