package com.example.payment.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        String currency
) {}
