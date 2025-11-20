# 🧩 Idempotency API — Spring Boot

A lightweight, production-grade example of **Idempotency**, **Rate Limiting**, **TTL-based caching**, and **JSON body normalization** using **Spring Boot 3**, Caffeine Cache, and Bucket4j.

This project demonstrates how to build safe and resilient HTTP APIs that prevent duplicated operations — especially useful for payments, order processing, or any operation that **must not be executed twice**.

---

## 🚀 Features

### ✅ Idempotency Filter
Implements idempotency at the HTTP layer using:

- `Idempotency-Key` header
- Protected HTTP methods (`POST`, `PUT`, `PATCH`, `DELETE`)
- Response caching based on  
  **`<method>:<path>:<idempotency-key>`**
- Fast response wrapping with `ContentCachingResponseWrapper`
- Proper charset handling
- Structured logging for idempotency hits and stores

---

### 🔒 Rate Limiting (Bucket4j)
- Built with **Bucket4j 8.x**
- Per-IP in-memory buckets
- Non-deprecated API (`Refill.intervally`)
- 429 responses when exceeded
- No manual `@Order` priority required

---

### ⏱ TTL-Based Idempotency Storage
Powered by **Caffeine Cache**:

- Per-key TTL eviction
- Highly optimized LRU memory management
- Near O(1) latency for GET/PUT
- Automatic cleanup of expired entries

---

### 🔧 JSON Body Normalization (Optional)
To prevent false positives when comparing request bodies:

- Normalizes JSON structure deterministically
- Removes whitespace
- Sorts keys alphabetically
- Graceful fallback when invalid JSON is received

Useful for implementing hash-based idempotency strategies.

---

### 🧾 Structured Logging
All components emit structured logs for observability:

- `payment.request_received`
- `payment.success`
- `payment.response_sent`
- `idempotency.hit`
- `idempotency.store`
- `rate_limit.block`

Compatible with ELK, Loki, Datadog, and CloudWatch.

---

## 📦 Tech Stack

- **Java 21**
- **Spring Boot 3.2**
- **Bucket4j 8.10**
- **Caffeine 3.1**
- **Jakarta Servlet API**
- **SLF4J structured logging**
- **Maven**

---

## 📂 Project Structure

src/main/java/org/example/idempotency
│
├── config/ # Idempotency properties (methods)
├── filter/ # IdempotencyFilter, RateLimitFilter
├── service/ # PaymentService (business logic)
├── controller/ # PaymentController
├── model/ # IdempotencyEntry
├── storage/ # IdempotencyStorage (Caffeine)
└── util/ # JsonNormalizer, HashUtils


---

## 🔑 How Idempotency Works

1. Client sends request with:

Idempotency-Key: abc-123


2. First request:
- Normal execution
- Response cached with TTL
- Log: `idempotency.store`

3. Subsequent requests:
- No processing performed
- Cached response returned instantly
- Log: `idempotency.hit`

Prevents duplicated payments or duplicated workflows.

---

## 📝 Example Request

```bash
curl --location 'http://localhost:8080/payments' \
--header 'Idempotency-Key: key-123' \
--header 'Content-Type: application/json' \
--data '{
 "orderId": "A1",
 "amount": 100,
 "currency": "USD"
}'
```
✔ First request → processed normally
✔ Next requests → instant response (cached)


⚙ Configuration
application.yml

idempotency:
  methods:
    - POST
    - PUT
    - PATCH
    - DELETE


TTL is configured directly in the Caffeine storage implementation.


