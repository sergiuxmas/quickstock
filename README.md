# QuickStock — Core + Payments (Java 17, Spring Boot, Maven, Postgres, Flyway)

This repository contains **two Spring Boot applications**:

- **quickstock-core-service** — Products, Inventory, Orders, Reservations/Expiration
- **payments-service** — Payments API (idempotent payment attempts + provider simulation)  
  Communicates with Core via REST callbacks (or can be upgraded to events later).

---

## Tech Stack

- Java **17**
- Maven (with **Maven Wrapper** `./mvnw`)
- Spring Boot (latest compatible with Java 17)
- PostgreSQL (Docker)
- Flyway (DB migrations)

---

## Repository Structure
