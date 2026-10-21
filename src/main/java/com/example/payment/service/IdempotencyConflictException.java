package com.example.payment.service;

public class IdempotencyConflictException extends RuntimeException{
    public IdempotencyConflictException() {
        super("Idempotency key is used with different payload.");
    }
}
