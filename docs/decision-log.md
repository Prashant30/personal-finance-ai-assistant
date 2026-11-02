# Decision Log

| Decision | Choice | Reason |
|---|---|---|
| Build | Maven Wrapper | Familiar enterprise build with pinned dependencies. |
| Development | VS Code Dev Container | No local Java or Maven installation and no cross-project version conflicts. |
| API style | REST + OpenAPI | Testable and shareable before investing in a UI. |
| Money | Integer cents | Avoid floating-point rounding and SQLite decimal-affinity ambiguity. |
| Dates | ISO-8601 text | Sortable and unambiguous in SQLite. |
| Data | Deterministic fictional CSV | Reproducible with no privacy or licensing concerns. |
| AI scope | Stateless controlled analytics | Small, testable POC; conversation memory is deferred. |
| Model access | SSH tunnel | Avoid exposing the vLLM HTTP server publicly. |
| vLLM reasoning | `--reasoning-parser qwen3` | vLLM 0.32 removed `--enable-reasoning`; the parser flag is sufficient. |
| Model API URL | Include `/v1` in `VLLM_BASE_URL` | Spring AI 2.0's OpenAI SDK appends `/chat/completions` to the configured API root. |
| CI | Deferred | First prove the local POC; add GitHub Actions during portfolio polishing. |

Major decisions and tradeoffs are recorded separately in [decisions](decisions/).

