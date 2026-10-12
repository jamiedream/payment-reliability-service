package com.example.payment.dto;

import com.example.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        BigDecimal amount,
        String currency,
        PaymentStatus status
) {
}
