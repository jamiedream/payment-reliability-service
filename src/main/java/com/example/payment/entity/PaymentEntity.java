package com.example.payment.entity;

import com.example.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payments_idempotency_key",
                columnNames = "idempotency_key"
        )
)
public class PaymentEntity {

    // unique與uniqueConstraints是重複宣告
    @Column(name = "idempotency_key", nullable = false, length = 100)
    public String idempotencyKey;
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    public String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public PaymentStatus status;

    protected PaymentEntity() {
        // JPA 需要無參數 Constructor 來建立 Entity Instance。它可以是 public 或 protected。
    }

    public PaymentEntity(
            UUID id,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String idempotencyKey
    ) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

}
