package com.example.payment.service;

import com.example.payment.domain.PaymentStatus;
import com.example.payment.dto.CreatePaymentResult;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.PaymentEntity;
import com.example.payment.repository.PaymentRepository;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository repository;

    @Inject
    PaymentWriter paymentWriter;

    @Transactional
    public CreatePaymentResult createPayment(PaymentRequest paymentRequest, String key) {
        PaymentEntity existing = repository.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            return reuseExisting(existing, paymentRequest);
        }

        try {
        PaymentEntity create = paymentWriter.insert(paymentRequest, key);

        return new CreatePaymentResult(toResponse(create), true);
        } catch (RuntimeException e) {
            if (!DatabaseConstraintHelper
                    .isIdempotencyConflict(e)) {
                throw e;
            }

            PaymentEntity concurrent =
                    repository.findByIdempotencyKey(key)
                            .orElse(null);

            if (concurrent == null) {
                throw e;
            }

            return reuseExisting(concurrent, paymentRequest);
        }
    }

    public PaymentResponse getPayment(UUID id) {
        PaymentEntity entity = repository.findById(id);

        if (entity == null) {
            return null;
        }

        return toResponse(entity);
    }

    private PaymentResponse toResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.id,
                entity.amount,
                entity.currency,
                entity.status
        );
    }

    private CreatePaymentResult reuseExisting(PaymentEntity existing, PaymentRequest request) {
        boolean isSamePayload = existing.amount.compareTo(request.amount()) == 0 && existing.currency.equals(request.currency());
        if (!isSamePayload) {
            throw new IdempotencyConflictException();
        }

        return new CreatePaymentResult(toResponse(existing), false);
    }
}
