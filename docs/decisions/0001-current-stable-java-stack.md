# ADR 0001: Current Stable Java Stack

- **Status:** Accepted
- **Decision:** Use Eclipse Temurin Java 25 LTS, Spring Boot 4.1.2, Spring AI 2.0.2, and Maven 3.9.11.
- **Reasons:** Java 25 is the current LTS; Spring Boot 4.1 supports it; Spring AI 2.0 is the current GA line compatible with Boot 4.1. Versions are pinned for reproducibility.
- **Tradeoff:** Many existing employers still run Java 17 or 21, but the core language and Spring patterns remain transferable.

