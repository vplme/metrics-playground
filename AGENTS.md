# Metrics Playground — Agent Context

## Project Overview

A sandbox project for experimenting with application metrics using OpenTelemetry and Prometheus.

### Architecture

Two Spring Boot 4.0.3 (Kotlin) applications communicating over HTTP, pushing metrics via OTLP directly to Prometheus, visualized in Grafana.

```
movie-client (8080) ──OTLP──► Prometheus (9090) ◄──OTLP── movie-service (8081)
       │                              │
       │  GET /movies, /movies/{id}   │
       └──────── HTTP ──────► movie-service (GET /api/movies, /api/movies/{id})
                                      │
                               Grafana (3000)
```

## Tech Stack & Versions

| Component | Version | Notes |
|---|---|---|
| Spring Boot | 4.0.3 | Major version — uses Spring Framework 7, modularized starters |
| Kotlin | 2.2.20 | As shipped with Spring Boot 4.0 |
| Java | 21 (toolchain) | Temurin 21.0.6 installed locally; Java 25 also available via mise |
| Gradle | 8.14 | Kotlin DSL, wrapper included |
| Micrometer | 1.16.x | Managed by Spring Boot BOM |
| Micrometer OTLP Registry | Managed | `io.micrometer:micrometer-registry-otlp` — pushes metrics via OTLP HTTP |
| Prometheus | latest (Docker) | Native OTLP write receiver (`--web.enable-otlp-receiver`) |
| Grafana | latest (Docker) | Auto-provisioned datasource and dashboard |

## Project Structure

```
metrics-playground/
├── build.gradle.kts                    # Root: plugin versions (Spring Boot 4.0.3, Kotlin 2.2.20)
├── settings.gradle.kts                 # Includes movie-service, movie-client
├── gradle.properties
├── gradlew / gradle/wrapper/
├── docker-compose.yml                  # 4 services: movie-service, movie-client, prometheus, grafana
├── .gitignore
│
├── movie-service/                      # Mock movie API provider
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src/main/
│       ├── kotlin/com/playground/metrics/service/
│       │   ├── MovieServiceApplication.kt
│       │   ├── controller/MovieApiController.kt
│       │   ├── model/Movie.kt
│       │   └── service/MovieService.kt       # 10 hardcoded movies, 50-500ms latency, ~10% error rate
│       └── resources/application.yml          # port 8081, OTLP export
│
├── movie-client/                       # API consumer / gateway
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src/main/
│       ├── kotlin/com/playground/metrics/client/
│       │   ├── MovieClientApplication.kt
│       │   ├── client/MovieApiClient.kt       # RestClient calling movie-service
│       │   ├── config/RestClientConfig.kt     # RestClient bean
│       │   ├── controller/MovieController.kt  # GET /movies, GET /movies/{id}
│       │   ├── metrics/CustomMetrics.kt       # movies.fetched, movies.fetch.errors, movies.fetch.duration
│       │   └── model/Movie.kt
│       └── resources/application.yml          # port 8080, OTLP export
│
├── prometheus/prometheus.yml           # OTLP receiver config, resource attribute promotion
└── grafana/provisioning/
    ├── datasources/prometheus.yml
    └── dashboards/
        ├── dashboard.yml
        └── app-metrics.json            # 8-panel dashboard
```

## Key Design Decisions

1. **OTLP push to Prometheus (no OTel Collector)** — Apps push metrics directly to Prometheus's native OTLP HTTP endpoint (`/api/v1/otlp/v1/metrics`). No scraping, no collector in between.

2. **Separate Spring Boot apps (not WireMock)** — The movie-service is a real Spring Boot app, not an embedded mock. This gives realistic inter-service HTTP metrics on both sides.

3. **Micrometer OTLP registry (not Prometheus registry)** — Using `micrometer-registry-otlp` for push-based export rather than `micrometer-registry-prometheus` with scraping.

4. **Spring Boot 4.0 specifics** — This is a major version with breaking changes from 3.x:
   - Uses Spring Framework 7.0
   - Modularized starters (e.g., `spring-boot-starter-opentelemetry` exists but we use micrometer-registry-otlp directly)
   - Kotlin 2.2.x (not 1.9 or 2.0)
   - Jackson 3 support (Jackson 2 deprecated)

## Metrics Pipeline

### How metrics flow
1. Both apps have `micrometer-registry-otlp` which periodically (every 10s) pushes metrics via OTLP HTTP POST
2. Prometheus receives on `/api/v1/otlp/v1/metrics` (enabled via `--web.enable-otlp-receiver`)
3. Prometheus config promotes `service.name`, `service.instance.id`, `service.namespace`, `service.version` as labels
4. Out-of-order ingestion enabled (30m window) for batched OTLP writes
5. Grafana reads from Prometheus

### Auto-instrumented metrics (via Micrometer + Spring Boot Actuator)
- `http.server.request.duration` — HTTP server request latency histogram
- `jvm.memory.used` — JVM heap/non-heap memory
- `jvm.gc.pause` — GC pause durations
- Various other JVM, system, and HTTP metrics

### Custom metrics (movie-client only, in CustomMetrics.kt)
- `movies.fetched` — Counter, total fetch operations
- `movies.fetch.errors` — Counter, failed fetches
- `movies.fetch.duration` — Timer with percentile histogram

## Prometheus Configuration

Key aspects of `prometheus/prometheus.yml`:
- `storage.tsdb.out_of_order_time_window: 30m` — required for OTLP batched writes
- `otlp.promote_resource_attributes` — promotes service.name etc. to metric labels
- Self-scrape only (no app scrape targets — all metrics arrive via OTLP push)

## Docker Compose

- `movie-service` — built from `movie-service/Dockerfile`, port 8081
- `movie-client` — built from `movie-client/Dockerfile`, port 8080, env: `MOVIE_SERVICE_URL=http://movie-service:8081`
- `prometheus` — `prom/prometheus:latest`, port 9090, flags: `--web.enable-otlp-receiver`
- `grafana` — `grafana/grafana:latest`, port 3000, anonymous admin access enabled

## Build & Run

```bash
# Build (requires Java 21):
JAVA_HOME=/Users/vpl/Library/Java/JavaVirtualMachines/temurin-21.0.6/Contents/Home ./gradlew build

# Run with Docker Compose:
docker compose up --build

# Run locally (two terminals):
JAVA_HOME=... ./gradlew :movie-service:bootRun
JAVA_HOME=... ./gradlew :movie-client:bootRun

# Test:
curl http://localhost:8080/movies
curl http://localhost:8080/movies/3
```

## Grafana Dashboard

Pre-provisioned at `grafana/provisioning/dashboards/app-metrics.json` with 8 panels:
1. HTTP Server Request Rate (movie-client)
2. HTTP Server Request Rate (movie-service)
3. HTTP Server Latency p50/p95/p99 (movie-client)
4. HTTP Server Latency p50/p95/p99 (movie-service)
5. HTTP Server Error Rate (5xx) — both services
6. Custom Movies Fetched & Errors (movie-client)
7. Custom Movie Fetch Duration percentiles (movie-client)
8. JVM Memory Usage — both services

**Note:** The dashboard uses hardcoded datasource UID `PBFA97CFB590B2093`. If Grafana assigns a different UID to the auto-provisioned Prometheus datasource, panels may need updating. The metric names in queries may also need adjustment depending on how OTLP translates them (underscore escaping, suffixes like `_total`, `_seconds`, `_bucket`).

## Known Considerations

- **No tests yet** — No unit or integration tests have been written.
- **No git repo initialized** — The project directory is not a git repository.
- **Dockerfiles use relative COPY paths** — The Dockerfiles assume `docker compose build` runs from the project root (which `docker-compose.yml` handles via `context: .`).
- **Java toolchain** — The build is configured for Java 21 via Gradle toolchain. Local machine has Temurin 21.0.6 and OpenJDK 25.
- **Metric name translation** — Prometheus's OTLP receiver applies `UnderscoreEscapingWithSuffixes` by default, so OTel metric names like `http.server.request.duration` become `http_server_request_duration_seconds`. Custom metrics like `movies.fetched` become `movies_fetched_total`. The Grafana dashboard queries assume this translation.
- **movie-service simulated failures** — `MovieService.kt` has `Thread.sleep()` for latency and a 10% random 500 error rate. These are intentional for generating interesting metrics.
