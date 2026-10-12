package com.example.payment.resource;

import com.example.ErrorResponse;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @POST
    public Response createPayment(PaymentRequest request) {

        if (request == null
                || request.amount() == null
                || request.amount().signum() <= 0
                || request.currency() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid payment request", "Amount must be greater than zero"))
                    .build();
        }

        PaymentResponse payment = paymentService.createPayment(request);

        return Response.status(201)
                .entity(payment)
                .build();
    }
}
