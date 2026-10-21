package com.example.payment.repository;

import com.example.payment.entity.PaymentEntity;

import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepository
    implements PanacheRepositoryBase<PaymentEntity, UUID> {

    public Optional<PaymentEntity> findByIdempotencyKey(String id) {

        return find("idempotencyKey", id).firstResultOptional();
    }
}
