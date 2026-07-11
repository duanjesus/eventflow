<div align="center">

# EventFlow

### Event-driven notification platform — RabbitMQ producer/consumer pipeline with retry, dead-lettering, Redis dedup/rate-limiting and full observability

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Prometheus](https://img.shields.io/badge/Prometheus-metrics-E6522C?logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-dashboards-F46800?logo=grafana&logoColor=white)](https://grafana.com/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](#license)

</div>

---

## 📖 About the project

**EventFlow** is a standalone event-driven notification service: a producer publishes a domain event (e.g. `expense.created`, `donation.created`) onto a RabbitMQ exchange, a consumer picks it up and fans it out to Email, Push and WebSocket channels, and every outcome — processed, failed, retried, dead-lettered, or skipped as a duplicate — is recorded and surfaced on a live React dashboard.

> This repo is intentionally **standalone**: it exposes its own `POST /api/v1/events` endpoint and a `/simulate` demo endpoint that publish sample `ExpenseCreated`/`DonationCreated` events, rather than being wired into the sibling CashPilot/Social Supply repos. The idea it demonstrates — a separate service reacting to domain events over a message broker instead of synchronous REST calls — is the same one that would let those repos call in for real later.

| Package | Description | Docs |
|---|---|---|
| [`backend/`](backend) | Spring Boot 3 API — RabbitMQ producer/consumer, retry + DLQ, Redis dedup/rate-limit/cache, Micrometer metrics | [backend source](backend/src/main/java/com/eventflow) |
| [`frontend/`](frontend) | React + TypeScript dashboard — live stats, recent events table, WebSocket notification feed | [frontend source](frontend/src) |
| [`monitoring/`](monitoring) | Prometheus scrape config + Grafana datasource/dashboard provisioning | |

---

## ✨ Features

```
✅ RabbitMQ producer / consumer

✅ Retry with exponential backoff

✅ Dead Letter Queue

✅ Email notifications (simulated)

✅ Push notifications (simulated)

✅ Real-time WebSocket notifications

✅ Redis deduplication

✅ Redis rate limiting

✅ Cached dashboard stats

✅ Prometheus metrics

✅ Grafana dashboards

✅ Health checks

✅ Docker

✅ CI/CD
```

---

## 🚀 Quick start (full stack, with Docker)

```bash
git clone https://github.com/duanjesus/eventflow.git
cd eventflow
docker compose up --build
```

| Service      | URL                                    |
|--------------|------------------------------------------|
| Dashboard    | http://localhost:3000                    |
| API          | http://localhost:8080                    |
| Swagger      | http://localhost:8080/swagger-ui.html    |
| RabbitMQ UI  | http://localhost:15672 (eventflow/eventflow) |
| Prometheus   | http://localhost:9090                    |
| Grafana      | http://localhost:3001 (admin/admin)      |

Open the dashboard, click **Simulate events** to publish a couple of sample `expense.created`/`donation.created` events, and watch the stat cards and live feed update. Click **Simulate failure** to publish an event with a `simulateFailure` flag — it exhausts its retries (visible ticking up in real time) and lands on the dead-letter queue, which you can also watch happen live in the RabbitMQ management UI.

## 🧪 Local development (without Docker)

```bash
# 1. Infra only
docker compose up -d db rabbitmq redis

# 2. Backend (terminal 1)
cd backend
mvn spring-boot:run

# 3. Frontend (terminal 2)
cd frontend
npm install
npm run dev
```

Frontend dev server: http://localhost:5173 (Vite proxies `/api` and `/ws` to `http://localhost:8080`).

Backend tests include a Testcontainers-based integration test that spins up real Postgres/RabbitMQ/Redis containers — it needs a working Docker daemon to run (`mvn test`), same as anything using Testcontainers.

---

## 🏗️ Architecture

```mermaid
flowchart LR
    Producer["POST /api/v1/events<br/>(or /simulate)"] -->|publish| Exchange{{eventflow.exchange<br/>topic exchange}}
    Exchange --> Queue[["eventflow.events.queue"]]
    Queue --> Consumer[EventConsumer]
    Consumer -->|dedup check| Redis[(Redis)]
    Consumer --> Dispatch[NotificationDispatchService]
    Dispatch --> Email[Email — simulated]
    Dispatch --> Push[Push — simulated]
    Dispatch --> WS[WebSocket — real, STOMP]
    Consumer -->|record outcome| DB[(PostgreSQL<br/>processed_events)]
    Queue -->|retries exhausted| DLX{{eventflow.exchange.dlx}}
    DLX --> DLQ[["eventflow.events.dlq"]]
    DLQ --> DeadLetterConsumer --> DB
    Consumer -->|metrics| Prom[Prometheus]
    Prom --> Grafana
    WS -.->|/topic/notifications| Dashboard[React Dashboard]
    DB -->|stats + recent events| Dashboard
```

**The retry + DLQ path:** `eventflow.events.queue` is declared with `x-dead-letter-exchange` pointing at `eventflow.exchange.dlx`. A Spring AMQP `RetryOperationsInterceptor` wraps the consumer, retrying a failing message in-process (exponential backoff) up to `eventflow.retry.max-attempts` times; once exhausted, `RejectAndDontRequeueRecoverer` rejects the message, and RabbitMQ routes it to the DLX automatically. A separate `DeadLetterConsumer` listens on the DLQ purely to record the terminal `DEAD_LETTERED` outcome for the dashboard.

**Deduplication is check-before / mark-after-success, not claim-then-process:** the Redis key for an eventId is only set once processing actually succeeds. Claiming it up front would make every in-process retry of a currently-failing delivery look like a duplicate of itself and get silently skipped instead of retried — see the note in `DeduplicationService`.

---

## 🗺️ Roadmap

- [x] **V1** — Producer, consumer, queue, retry with exponential backoff, Dead Letter Queue
- [x] **V2** — Email, Push (simulated) and real WebSocket notification channels
- [x] **V3** — Redis-backed deduplication, per-source rate limiting, cached dashboard stats
- [x] **V4** — Micrometer metrics, Prometheus scrape endpoint, provisioned Grafana dashboard, Actuator health checks

---

## 🏗️ Repository layout

```
eventflow/
├── backend/                     Spring Boot API (Java 21, RabbitMQ, Redis, PostgreSQL/Flyway)
│   ├── src/main/java/com/eventflow/
│   │   ├── config/              RabbitMQ, WebSocket, typed @ConfigurationProperties
│   │   ├── event/                DomainEvent record
│   │   ├── producer/            EventPublisher
│   │   ├── consumer/            EventConsumer, DeadLetterConsumer
│   │   ├── notification/        NotificationChannel + Email/Push/WebSocket impls
│   │   ├── service/              EventProcessingService, Deduplication/RateLimit/DashboardStats
│   │   ├── entity/ repository/   ProcessedEvent (Postgres, one row per eventId)
│   │   ├── controller/          EventController, DashboardController
│   │   └── metrics/              EventMetrics (Micrometer counters/timer)
│   ├── src/test/java/            Unit tests (JUnit5+Mockito) + EventFlowIntegrationTest (Testcontainers)
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                    React + TypeScript dashboard (Vite, Tailwind, TanStack Query, STOMP.js)
├── monitoring/
│   ├── prometheus/prometheus.yml
│   └── grafana/provisioning/    Datasource + one provisioned dashboard (EventFlow Overview)
├── docker-compose.yml           db + rabbitmq + redis + api + web + prometheus + grafana
└── .github/workflows/ci.yml     Backend (Maven+Testcontainers) + Frontend (npm) jobs
```

---

## 🌱 Commit convention

This project follows **Conventional Commits** (`feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`).

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
