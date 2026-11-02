# Vast.ai and vLLM Setup

Rent the GPU only after local tests pass. A single RTX 3090 with 24 GB VRAM, 32 GB system RAM, and 50 GB disk ran Qwen3-8B at an 8K context length for this POC.

## 1. Verify the remote environment

Connect using the SSH command returned by:

```bash
vastai ssh-url INSTANCE_ID
```

On the instance:

```bash
nvidia-smi
python3 --version
command -v vllm || true
df -h /
```

Install vLLM only if the selected template does not already provide it.

## 2. Start vLLM remotely

Generate a temporary API key and keep it out of Git:

```bash
export VLLM_API_KEY="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export MODEL_NAME=Qwen/Qwen3-8B
```

Run `scripts/vast/start-vllm.sh` on the remote host, or use its command directly. The server binds to remote loopback only.

## 3. Open the tunnel

On the Mac, when running the app with Docker Compose:

```bash
ssh -N -L 8000:127.0.0.1:8000 -p SSH_PORT root@SSH_HOST
```

Compose uses `http://host.docker.internal:8000/v1` to reach this tunnel. If running the app directly inside the Dev Container, open the tunnel inside a second Dev Container terminal instead:

```bash
ssh -N -L 8000:127.0.0.1:8000 -p SSH_PORT root@SSH_HOST
```

The `-L` option must appear before the remote destination. Keep the terminal open.

## 4. Verify and run

In the same environment where the tunnel is open, verify the model:

```bash
export VLLM_API_KEY='the-same-temporary-key'
curl http://localhost:8000/v1/models \
  -H "Authorization: Bearer $VLLM_API_KEY"
```

For Docker Compose, save the key in the ignored `.env` file and run `docker compose up -d --force-recreate` on the Mac. For direct execution inside the Dev Container, run `./mvnw spring-boot:run` there with the exported key. Then run the example request in the README and compare its evidence to a direct transaction query.

## 5. Control cost

- Keep code and the SQLite database local.
- Use the GPU only for live integration sessions.
- Stop vLLM when testing ends.
- Destroy the Vast.ai instance after saving any useful logs; stopped instances still incur storage charges.
- Never commit Vast.ai credentials, model API keys, or SSH private keys.

