# ADR 0002: Containerized Development

- **Status:** Accepted
- **Decision:** Put Java, Maven, and SSH tooling in a VS Code Dev Container and provide a production-style Dockerfile.
- **Reasons:** The Mac needs no JDK installation, projects cannot conflict over Java versions, and reviewers receive a reproducible environment.
- **Tradeoff:** Initial image download and IDE container startup are slower than using an existing local JDK.

