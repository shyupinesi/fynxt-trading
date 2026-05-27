# FYNXT – Order Booking & Portfolio API

A production-grade Spring Boot backend for a stock trading desk.
Handles concurrent order placement, filling/cancellation, portfolio tracking, and sector risk analysis.

---

## Tech Stack

| Layer       | Choice                          |
|-------------|---------------------------------|
| Language    | Java 17                         |
| Framework   | Spring Boot 3.2.5               |
| Persistence | Spring Data JPA + H2 (in-memory)|
| Validation  | Bean Validation (jakarta)       |
| Testing     | JUnit 5, Mockito, MockMvc       |
| Build       | Maven                           |

---

## How to Run

### Prerequisites
- JDK 17+
- Maven 3.8+ (or use the included `./mvnw` wrapper)

### Start the application

```bash
cd fynxt-trading
./mvnw spring-boot:run
```

The server starts at **http://localhost:8080**

### H2 Console (dev only)
Navigate to **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:tradingdb`
- Username: `sa`  Password: *(empty)*

### Run all tests

```bash
./mvnw test
```

---

## API Reference

### 1. Place an Order
```
POST /api/orders
Content-Type: application/json

{
  "traderId": "T001",
  "stock": "AAPL",
  "sector": "TECH",
  "quantity": 50,
  "side": "BUY"
}
```
**Business rules enforced:**
- Trader must exist
- Trader cannot have > 3 PENDING orders simultaneously
- SELL is rejected if trader holds fewer shares than requested

---

### 2. Fill an Order
```
PATCH /api/orders/{id}/fill
```
- Transitions `PENDING → FILLED`
- Updates the portfolio (BUY increases, SELL decreases holdings)
- Returns `422` with a descriptive message for any non-PENDING order

---

### 3. Cancel an Order
```
PATCH /api/orders/{id}/cancel
```
- Transitions `PENDING → CANCELLED`
- Returns `422` with the current status in the error message for non-PENDING orders

---

### 4. Get Portfolio
```
GET /api/traders/{traderId}/portfolio
```
**Response:**
```json
{
  "traderId": "T001",
  "positions": { "AAPL": 150, "TSLA": 80 },
  "sectorBreakdown": { "TECH": 230 }
}
```

---

### 5. Sector Overlap Analysis
```
GET /api/traders/{traderId}/portfolio/overlap
```
**Response:**
```json
{
  "overlaps": [
    { "basket": "BALANCED",      "overlap": "50.00%" },
    { "basket": "FINANCE_HEAVY", "overlap": "0.00%"  },
    { "basket": "TECH_HEAVY",    "overlap": "75.00%" }
  ],
  "dominantBasket": "TECH_HEAVY",
  "riskFlag": "HIGH"
}
```

---

### 6. Add to Portfolio
```
POST /api/traders/{traderId}/portfolio
Content-Type: application/json

{ "stock": "NVDA", "sector": "TECH", "quantity": 100 }
```

---

## Seeded Traders
Traders `T001`, `T002`, `T003` are pre-created by `data.sql`.

---

## Key Design Decisions

### Concurrency
Two strategies are layered for correctness under concurrent load:

1. **Pessimistic locking (`PESSIMISTIC_WRITE`)** on the `Order` row when filling or
   cancelling. This serialises concurrent fill attempts on the same order at the DB
   level — the second transaction blocks until the first commits, then finds the order
   in a non-PENDING state and raises a `BusinessRuleException`.

2. **Same lock on `PortfolioPosition`** during portfolio mutations. This prevents
   phantom reads and lost updates when two fills for the same (trader, stock) pair
   execute simultaneously.

> **Why not optimistic locking?**
> Optimistic locking with `@Version` is fine for low-contention scenarios but forces
> the caller to retry on `OptimisticLockException`. Given the assignment's emphasis on
> correctness under *concurrent load*, pessimistic locking gives stronger guarantees
> with simpler client-side semantics.

### Pure-Java Sector Overlap (`SectorOverlapAnalyzer`)
The analyser has zero DB calls and zero Spring dependencies beyond `@Component`.
Baskets are defined as `Map<String, Set<String>>` constants; the Dice-coefficient formula
runs as a single stream operation. This makes it trivially unit-testable and safe to
call from any thread.

### Error Handling
All domain errors map to a consistent `ApiErrorResponse` body via `GlobalExceptionHandler`:
- `404` → `ResourceNotFoundException`
- `422` → `BusinessRuleException` (business rule violations, invalid state transitions)
- `400` → Bean Validation failures
- `500` → Unexpected errors (message is sanitised — stack trace is never exposed)

### Clean Architecture
```
controller  →  service  →  repository  →  entity
                 ↓
            SectorOverlapAnalyzer (pure Java)
```
Controllers are thin (validation + delegation only).  
Services own all business logic and transaction boundaries.  
Repositories are interfaces — no custom SQL beyond two locking queries.

---

## Trade-offs & Intentional Shortcuts

| Area | Decision | Reason |
|------|----------|--------|
| Database | H2 in-memory | Zero setup; swap `application.properties` for PostgreSQL |
| Auth | None | Out of scope for the assignment |
| Pagination | Not implemented | No volume requirements specified |
| Trader creation | Seeded via `data.sql` | Assignment focuses on order/portfolio logic |
| Audit log | Not implemented | Would add `OrderAuditEvent` table in production |

---

## Project Structure

```
src/
├── main/java/com/fynxt/trading/
│   ├── TradingApplication.java
│   ├── controller/
│   │   ├── OrderController.java
│   │   └── PortfolioController.java
│   ├── service/
│   │   ├── OrderService.java
│   │   ├── PortfolioService.java
│   │   ├── SectorOverlapAnalyzer.java   ← pure Java, no DB
│   │   └── OrderMapper.java
│   ├── repository/
│   │   ├── OrderRepository.java
│   │   ├── PortfolioRepository.java
│   │   └── TraderRepository.java
│   ├── entity/
│   │   ├── Order.java
│   │   ├── PortfolioPosition.java
│   │   └── Trader.java
│   ├── dto/         ← request/response shapes
│   ├── enums/       ← OrderStatus, OrderSide
│   └── exception/   ← typed exceptions + GlobalExceptionHandler
├── main/resources/
│   ├── application.properties
│   ├── schema.sql
│   └── data.sql
└── test/
    ├── service/
    │   ├── SectorOverlapAnalyzerTest.java   ← unit tests (no Spring context)
    │   └── OrderServiceTest.java            ← unit tests with Mockito
    ├── controller/
    │   ├── OrderControllerIntegrationTest.java
    │   └── PortfolioControllerIntegrationTest.java
    └── concurrency/
        └── OrderConcurrencyTest.java        ← concurrent load tests
```

