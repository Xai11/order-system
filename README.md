# Order Processing System

Event-driven order processing system built with Java 17, Spring Boot 3.2, Apache Kafka (KRaft), and PostgreSQL. Designed for generating business events to test and calibrate monitoring tools.

## Architecture

```
POST /orders
     │
     ▼
order-service (8081) ──► [orders topic] ──► payment-service (8082) ──► [payments topic] ──► shipping-service (8083) ──► [shipments topic]
                                                                                                                                │
                         analytics-service (8084) ◄── [orders + payments + shipments topics] ──────────────────────────────────┘
                                   │
                              PostgreSQL
```

## Prerequisites

- Docker Desktop (or Docker Engine + Compose v2)
- Java 17
- Maven 3.9+

## Option A — Full Docker (recommended)

```bash
cd order-system
mvn clean package -DskipTests
docker compose up --build
```

Wait for all services to become healthy (~60s on first run due to image pulls and Kafka startup).

## Option B — Local dev (Kafka in Docker, services local)

```bash
# Start infrastructure
docker compose up kafka postgres kafka-ui -d

# Wait for Kafka healthcheck (check with: docker compose ps)
# Then start each service in a separate terminal:

cd order-service   && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd shipping-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
```

Local services connect to Kafka on `localhost:9094` (EXTERNAL listener).

## Verify

```bash
bash test-flow.sh
```

## Endpoints

### order-service (8081)
| Method | Path | Description |
|--------|------|-------------|
| POST | /orders | Create a new order |
| GET | /orders/{orderId} | Get order by ID |
| GET | /orders | List all orders |
| GET | /actuator/health | Health check |
| GET | /actuator/prometheus | Prometheus metrics |

**POST /orders request:**
```json
{ "userId": "user-1", "amount": 150.00 }
```

### analytics-service (8084)
| Method | Path | Description |
|--------|------|-------------|
| GET | /analytics/status-counts | Order counts by status |
| GET | /analytics/latency | Avg and P95 order→payment latency (ms) |
| GET | /analytics/failures | Payment failure breakdown by reason |
| GET | /analytics/summary | All of the above combined |

### Infrastructure
- **Kafka UI**: http://localhost:8090
- **Prometheus metrics** available on all services at `/actuator/prometheus`

## Quick test

```bash
# Create an order
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-1","amount":150.00}'

# Watch analytics (wait ~2s for pipeline to process)
curl http://localhost:8084/analytics/summary
```

## Event Flow

1. `POST /orders` → order-service stores order in memory, publishes `OrderCreated` to `orders` topic
2. payment-service consumes `orders`, simulates 500ms processing, publishes `PaymentResult` (80% PAID / 20% FAILED) to `payments` topic
3. shipping-service consumes `payments`, skips FAILED, simulates 200ms processing for PAID orders, publishes `ShipmentCreated` to `shipments` topic
4. analytics-service consumes all three topics, maintains in-memory counters, flushes aggregates to PostgreSQL every 10s

## Kafka Topics

| Topic | Partitions | Key |
|-------|-----------|-----|
| orders | 3 | orderId |
| payments | 3 | orderId |
| shipments | 3 | orderId |

## Payment Failure Reasons

- `INSUFFICIENT_FUNDS`
- `TIMEOUT`
- `CARD_DECLINED`

## Carriers

`DHL`, `FedEx`, `UPS`, `CDEK`

---

## Deployment

### Требования к окружению

| Инструмент | Версия |
|------------|--------|
| Docker Engine | 24+ |
| Docker Compose | v2.20+ |
| Java | 17 |
| Maven | 3.9+ |
| Свободное место на диске | 5 GB (образы + кэш Maven) |

---

### Шаг 1 — Клонировать репозиторий

```bash
git clone <repo-url>
cd order-system
```

---

### Шаг 2 — Собрать JAR-файлы

```bash
mvn clean package -DskipTests
```

Артефакты появятся в `*/target/*.jar` каждого модуля.

---

### Шаг 3 — Запустить стек

```bash
docker compose up --build -d
```

Флаг `-d` запускает сервисы в фоне. При первом запуске Docker скачает образы (~1 GB), это займёт 1–2 минуты.

---

### Шаг 4 — Дождаться готовности

Kafka стартует медленнее всего — другие сервисы ждут её `healthcheck`:

```bash
# Следить за статусом всех сервисов
docker compose ps

# Ожидаемый результат через ~60 секунд:
# kafka              running (healthy)
# postgres           running (healthy)
# kafka-ui           running
# order-service      running
# payment-service    running
# shipping-service   running
# analytics-service  running
```

Смотреть логи запуска:

```bash
docker compose logs -f
```

---

### Шаг 5 — Проверить работоспособность

```bash
bash test-flow.sh
```

Или вручную:

```bash
# Health checks всех сервисов
for port in 8081 8082 8083 8084; do
  echo -n "Port $port: "
  curl -s http://localhost:$port/actuator/health | grep -o '"status":"[^"]*"'
done

# Создать тестовый заказ
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-1","amount":250.00}'

# Проверить аналитику (~3 секунды на обработку)
sleep 3 && curl -s http://localhost:8084/analytics/summary
```

---

### Управление стеком

```bash
# Остановить все сервисы (данные сохраняются)
docker compose stop

# Запустить снова
docker compose start

# Пересобрать и перезапустить один сервис
docker compose up --build -d payment-service

# Полная остановка с удалением контейнеров и volumes
docker compose down -v

# Просмотр логов конкретного сервиса
docker compose logs -f payment-service
```

---

### Симуляция инцидентов (latency injection)

`payment-service` и `shipping-service` поддерживают динамическое управление задержкой без перезапуска:

```bash
# Посмотреть текущую задержку
curl http://localhost:8082/admin/delay   # payment-service (default: 500ms)
curl http://localhost:8083/admin/delay   # shipping-service (default: 200ms)

# Симулировать деградацию payment-service
curl -X POST http://localhost:8082/admin/delay \
  -H "Content-Type: application/json" \
  -d '{"ms": 5000}'

# Симулировать критический инцидент
curl -X POST http://localhost:8082/admin/delay \
  -H "Content-Type: application/json" \
  -d '{"ms": 30000}'

# Каскадная деградация обоих сервисов
curl -X POST http://localhost:8082/admin/delay -H "Content-Type: application/json" -d '{"ms": 8000}'
curl -X POST http://localhost:8083/admin/delay -H "Content-Type: application/json" -d '{"ms": 5000}'

# Вернуть в норму
curl -X POST http://localhost:8082/admin/delay -H "Content-Type: application/json" -d '{"ms": 500}'
curl -X POST http://localhost:8083/admin/delay -H "Content-Type: application/json" -d '{"ms": 200}'
```

Эффект виден в аналитике:

```bash
# Следить за P95 latency в реальном времени
watch -n 2 'curl -s http://localhost:8084/analytics/latency'
```

---

### Возможные проблемы

**`no space left on device` при сборке**

`/var` заполнен Docker-кэшем. Очистить и перезапустить:

```bash
docker system prune -af
docker builder prune -af
docker compose up --build -d
```

Если проблема повторяется — переместить Docker-данные на раздел с бо́льшим свободным местом (например, `/home`):

```bash
sudo systemctl stop docker
sudo mkdir -p /home/docker-data
sudo tee /etc/docker/daemon.json <<EOF
{ "data-root": "/home/docker-data" }
EOF
sudo rsync -aP /var/lib/docker/ /home/docker-data/
sudo systemctl start docker
```

**Kafka не становится `healthy`**

```bash
# Проверить логи Kafka
docker compose logs kafka | tail -30

# Принудительно пересоздать контейнер
docker compose rm -f kafka
docker compose up -d kafka
```

**analytics-service не стартует**

Зависит от Kafka И PostgreSQL. Убедиться, что оба `healthy`:

```bash
docker compose ps kafka postgres
```
