# Order Processing System — Claude Context

## Project Purpose

Event-driven order processing system for generating business events to test and calibrate monitoring tools. Not a production system — no auth, minimal error recovery.

## Module Layout

```
order-system/
├── pom.xml                  Parent POM (Java 17, Spring Boot 3.2.5)
├── docker-compose.yml       Full stack: kafka, postgres, kafka-ui, 4 services
├── order-service/           REST API → publishes to "orders" topic
├── payment-service/         Consumes "orders" → publishes to "payments" topic
├── shipping-service/        Consumes "payments" (PAID only) → publishes to "shipments"
├── analytics-service/       Consumes all 3 topics, writes aggregates to PostgreSQL
└── test-flow.sh             Integration smoke test
```

## Key Design Decisions

- **No Lombok** — all POJOs have explicit getters/setters
- **Constructor injection** everywhere — no `@Autowired` on fields
- **In-memory state** for order-service (ConcurrentHashMap), analytics-service (AtomicLong counters)
- **Manual Kafka ACK** (`MANUAL_IMMEDIATE`) on all consumers — never loses messages silently
- **Kafka topics created programmatically** via `NewTopic @Bean` — auto-create is disabled
- **KRaft mode** (no Zookeeper) — bitnami/kafka:3.7 with EXTERNAL listener on 9094 for local dev
- **Two application.yml profiles**: `application.yml` (localhost:9094) and `application-docker.yml` (kafka:9092)
- **Flyway migrations** in analytics-service only (the only service with a DB)

## Ports

| Service | Port |
|---------|------|
| order-service | 8081 |
| payment-service | 8082 |
| shipping-service | 8083 |
| analytics-service | 8084 |
| Kafka (external) | 9094 |
| PostgreSQL | 5432 |
| Kafka UI | 8090 |

## Build

```bash
mvn clean package -DskipTests          # build all modules
docker compose up --build              # full stack
```

## Common Issues

- Kafka healthcheck takes 30–60s on first boot — services wait for `service_healthy`
- analytics-service needs both Kafka AND postgres healthy before starting
- Local dev: use `localhost:9094` (EXTERNAL listener), Docker: use `kafka:9092` (PLAINTEXT)
- Each Dockerfile copies the parent `pom.xml` first, then the specific module pom, for layer caching
