package com.example.payment.service;

import com.example.payment.domain.PaymentStatus;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.entity.PaymentEntity;
import com.example.payment.repository.PaymentRepository;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentWriter {

    @Inject
    PaymentRepository repository;

    @Transactional
    public PaymentEntity insert(
            PaymentRequest request,
            String key
    ) {

        PaymentEntity entity = new PaymentEntity(
                UUID.randomUUID(),
                request.amount(),
                request.currency(),
                PaymentStatus.PENDING,
                key
        );


        repository.persistAndFlush(entity);

        return entity;
    }
}
