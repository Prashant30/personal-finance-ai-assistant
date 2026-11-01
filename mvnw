#!/bin/sh
set -eu

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

echo "Maven is not installed. Open this project in its Dev Container, then rerun ./mvnw." >&2
exit 1

