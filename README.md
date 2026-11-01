# Personal Finance AI Assistant

An evidence-backed personal finance assistant built with Java, Spring Boot, SQLite, Spring AI, and an open-source Qwen model served by vLLM.

The project uses **fictional data only**. It is educational and does not provide financial advice.

## What the POC demonstrates

- A Java 25 and Spring Boot 4 REST API.
- Embedded SQLite with versioned Flyway migrations.
- Exact money calculations using integer cents.
- Natural-language questions converted to controlled, validated analytics intents.
- Database-calculated evidence that the LLM may explain but never alter.
- An OpenAI-compatible connection to Qwen3-8B on a temporary Vast.ai GPU.

## Architecture

```text
REST client -> Spring Boot -> structured intent -> validated SQLite query
                    |                                  |
                    +------ vLLM / Qwen3-8B <----------+
                              explanation receives evidence only
```

See [architecture.md](docs/architecture.md) and the [architecture decision records](docs/decisions/).

## Run locally

Prerequisites: Docker Desktop, VS Code, and the Dev Containers extension. Java and Maven are provided by the container.

1. Open this folder in VS Code.
2. Select **Dev Containers: Reopen in Container**.
3. Run:

```bash
./mvnw spring-boot:run
```

The demo profile creates `finance.db` and loads deterministic synthetic transactions. Open:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

List October grocery transactions:

```bash
curl 'http://localhost:8080/api/v1/transactions?startDate=2026-10-01&endDate=2026-10-31&category=GROCERIES'
```

The transaction API works without a GPU. The assistant endpoint returns `503 AI_SERVICE_UNAVAILABLE` until vLLM is connected.

## Connect the live model

Follow [Vast.ai setup](docs/vast-ai.md). After opening the SSH tunnel inside the Dev Container, export the model configuration and restart Spring Boot:

```bash
export VLLM_BASE_URL=http://localhost:8000
export VLLM_API_KEY='your-temporary-key'
export VLLM_MODEL=Qwen/Qwen3-8B
./mvnw spring-boot:run
```

Ask a supported question:

```bash
curl -X POST http://localhost:8080/api/v1/assistant/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"How much did I spend on groceries in October 2026?"}'
```

Supported POC questions cover total spending, category breakdowns, top merchants, and month comparison.

## Test and package

```bash
./mvnw test
docker compose up --build
```

Automated tests use temporary SQLite files and mocked AI collaborators. They never require a GPU or paid service.

## Documentation

- [Architecture](docs/architecture.md)
- [Decision log](docs/decision-log.md)
- [Roadmap](docs/roadmap.md)
- [Demo script](docs/demo.md)
- [Vast.ai setup](docs/vast-ai.md)

## License

[MIT](LICENSE)

