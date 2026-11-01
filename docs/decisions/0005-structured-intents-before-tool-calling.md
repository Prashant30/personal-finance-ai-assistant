# ADR 0005: Structured Intents Before Tool Calling

- **Status:** Accepted
- **Decision:** The LLM maps questions to a fixed intent schema; Java validates and executes repository-owned queries.
- **Reasons:** This prevents arbitrary SQL, makes answers traceable, and is easier to test than an autonomous tool loop. The model explains evidence but never calculates financial totals.
- **Tradeoff:** The POC supports fewer question types. Controlled tool calling can be introduced after evaluation coverage exists.

