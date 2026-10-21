package com.example.payment.service;

import java.sql.SQLException;

public class DatabaseConstraintHelper {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String IDEMPOTENCY_CONSTRAINT =
            "uk_payments_idempotency_key";

    private DatabaseConstraintHelper() {
    }

    public static boolean isIdempotencyConflict(Throwable error) {
        for (Throwable cause = error;
             cause != null;
             cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                if (IDEMPOTENCY_CONSTRAINT.equals(violation.getConstraintName())) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException && UNIQUE_VIOLATION.equals(sqlException.getSQLState())) {
                // SQLSTATE alone identifies a unique violation,
                // not necessarily which constraint failed.
                // Continue looking for the named constraint.
            }
        }

        return false;
    }
}
