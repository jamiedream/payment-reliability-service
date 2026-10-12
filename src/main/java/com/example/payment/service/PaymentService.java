package com.example.payment.service;

import com.example.payment.domain.PaymentStatus;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentService {

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        return new PaymentResponse(
                UUID.randomUUID(),
                paymentRequest.amount(),
                paymentRequest.currency(),
                PaymentStatus.PENDING
        );
    }
}
