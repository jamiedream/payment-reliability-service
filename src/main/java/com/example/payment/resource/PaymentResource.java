package com.example.payment.resource;

import com.example.ErrorResponse;
import com.example.payment.dto.CreatePaymentResult;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;

import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
    public Response createPayment(@HeaderParam("Idempotency-Key") String key,  PaymentRequest request) {

        if (key == null
                || key.isBlank()
                || key.length() > 100) {

            return Response.status(400)
                    .entity(new ErrorResponse(
                            "INVALID_IDEMPOTENCY_KEY",
                            "Invalid Idempotency-Key"
                    ))
                    .build();
        }

        if (request == null
                || request.amount() == null
                || request.amount().signum() <= 0
                || request.currency() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid payment request", "Invalid payment request"))
                    .build();
        }

        CreatePaymentResult result = paymentService.createPayment(request, key);

        Response.Status status = result.created()? Response.Status.CREATED: Response.Status.OK;

        return Response.status(status)
                .entity(result.payment())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getPayment(@PathParam("id") UUID id) {

        PaymentResponse payment = paymentService.getPayment(id);

        if (payment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(
                            "PAYMENT_NOT_FOUND",
                            "Payment does not exist"
                    ))
                    .build();
        }

        return Response.ok(payment).build();
    }
}
