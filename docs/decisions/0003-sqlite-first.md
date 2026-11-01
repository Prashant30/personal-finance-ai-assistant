# ADR 0003: SQLite First

- **Status:** Accepted
- **Decision:** Use embedded SQLite with Spring `JdbcClient` and Flyway rather than PostgreSQL for the POC.
- **Reasons:** The POC is single-user and read-heavy. SQLite removes a database service, credentials, port, and container while keeping real SQL and migrations.
- **Tradeoff:** SQLite is not the intended store for high write concurrency or horizontally scaled services. Migrate to PostgreSQL when the application becomes multi-user, hosted, or directly ingests bank data.

