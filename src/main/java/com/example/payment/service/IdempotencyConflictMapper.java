package com.example.payment.service;

import com.example.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IdempotencyConflictMapper
        implements ExceptionMapper<IdempotencyConflictException> {

    @Override
    public Response toResponse(
            IdempotencyConflictException exception) {

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(
                        "IDEMPOTENCY_KEY_CONFLICT",
                        exception.getMessage()
                ))
                .build();
    }
}

