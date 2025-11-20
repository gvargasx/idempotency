package org.example.idempotency.service;

import org.example.idempotency.dto.PaymentRequest;
import org.example.idempotency.dto.PaymentResponse;
import org.example.idempotency.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    public PaymentResponse process(PaymentRequest request) {
        LOGGER.info("payment.start orderId={} amount={} currency={}",
                request.orderId(), request.amount(), request.currency());

        simulateExternalPaymentGateway();

        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                request.orderId(),
                request.amount(),
                request.currency(),
                "AUTHORIZED"
        );

        LOGGER.info("payment.success paymentId={} orderId={}",
                payment.id(), payment.orderId());

        return new PaymentResponse(
                payment.id(),
                payment.orderId(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                HttpStatus.OK
        );
    }

    private void simulateExternalPaymentGateway() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.error("payment.gateway_interrupted");
            throw new IllegalStateException("Payment Gateway simulation interrupted", ex);
        }
    }
}
