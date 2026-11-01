# Roadmap

## POC — current

- Read and filter fictional transactions.
- Ask stateless spending questions.
- Return database-calculated evidence.
- Run Qwen3-8B temporarily on Vast.ai.

## Next: low-cost improvements

- CSV upload with validation, preview, and duplicate detection.
- AI-assisted categorization with a labelled evaluation dataset.
- Monthly insights generated from deterministic aggregates.
- GitHub Actions for build and tests.

## Product improvements

- Conversation sessions and follow-up questions.
- Web or mobile UI.
- Budgets and unusual-spending alerts.
- Authentication and strict user-level data isolation.

## Higher-complexity integrations

- Bank-data provider abstraction and consent flow.
- Secure token storage and background synchronization.
- PostgreSQL migration for concurrent users and hosted deployment.
- Monitoring, model evaluations, rate limits, and cost controls.

RAG is intentionally deferred. Structured SQL is the correct retrieval mechanism for transactions; vector search becomes useful only if statements, policies, receipts, or other documents are added.

