# ADR 0004: Direct Java-to-Model Integration

- **Status:** Accepted
- **Decision:** Spring Boot calls vLLM's OpenAI-compatible API directly through Spring AI.
- **Reasons:** A Python gateway would add a network hop and deployment unit without adding POC capability. Python remains appropriate later for offline model evaluation.
- **Tradeoff:** Java owns AI orchestration, so model-specific experimentation is less flexible than in a Python notebook.

