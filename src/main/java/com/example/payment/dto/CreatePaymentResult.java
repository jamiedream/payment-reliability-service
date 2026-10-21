package com.example.payment.dto;

public record CreatePaymentResult(
        PaymentResponse payment,
        boolean created
) {}
