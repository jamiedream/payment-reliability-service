# Payment Reliability Service - Idempotent Payment Record Creation

A backend learning project demo payment record creation, persistence, and idempotent request handling using Java, Quarkus, and PostgreSQL.

## Tech Stack

- Java 21
- Quarkus
- Hibernate ORM with Panache
- PostgreSQL
- Jakarta Transactions
- JUnit 5 and REST Assured

## Features

- Create a payment record (`POST /payments`)
- Retrieve a payment record (`GET /payments/{id}`)
- Validate payment amount and currency
- Prevent duplicate payment records using an `Idempotency-Key`
- Reject reuse of an idempotency key with a different payload
- Use a PostgreSQL unique constraint for concurrent request safety

## API Behavior

| Scenario                       | Expected HTTP Status |
| ------------------------------ | -------------------- |
| New payment request            | 201 Created          |
| Same key and same payload      | 200 OK               |
| Same key and different payload | 409 Conflict         |
| Payment not found              | 404 Not Found        |

## Architecture

`PaymentResource → PaymentService → PaymentRepository / PaymentWriter → PostgreSQL`

## Running Locally

Prerequisites: Java 21 and Docker.

```bash
./gradlew quarkusDev
```

Run tests:

```bash
./gradlew test
```

## Design Decisions

**Database-backed idempotency:** A unique constraint on `idempotency_key` prevents duplicate payment records across concurrent requests and application instances sharing the same database.

**Transaction boundaries:** Database writes are performed in a dedicated transactional component. Unique constraint violations can be handled after the failed transaction has ended.

**Payload comparison:** Reusing an idempotency key with a different payment amount or currency returns a conflict.

## Limitations

- This project creates payment records but does not execute real payments.
- Idempotency keys are globally scoped in this demo.
- Payload comparison covers amount and currency only.
- No external payment provider, message queue, or recovery worker is integrated.
