package org.example.idempotency.controller;

import org.example.idempotency.dto.PaymentRequest;
import org.example.idempotency.dto.PaymentResponse;
import org.example.idempotency.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        LOGGER.info("payment.request_received orderId={} amount={} currency={}",
                request.orderId(), request.amount(), request.currency());

        PaymentResponse response = service.process(request);

        LOGGER.info("payment.response_sent paymentId={} orderId={} status={}",
                response.paymentId(), response.orderId(), response.status());

        return ResponseEntity
                .status(response.httpStatus().value())
                .body(response);
    }
}
