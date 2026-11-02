#!/usr/bin/env bash
set -euo pipefail

: "${VLLM_API_KEY:?Set VLLM_API_KEY before starting the server}"

MODEL_NAME="${MODEL_NAME:-Qwen/Qwen3-8B}"

exec vllm serve "$MODEL_NAME" \
  --host 127.0.0.1 \
  --port 8000 \
  --api-key "$VLLM_API_KEY" \
  --max-model-len 8192 \
  --reasoning-parser qwen3

