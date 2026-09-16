#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

BACKEND_PORT="${BACKEND_PORT:-8080}"
ML_PORT="${ML_PORT:-8000}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

cleanup() {
  echo
  echo "Stopping FourthDown AI services..."

  if [[ -n "${BACKEND_PID:-}" ]]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi

  if [[ -n "${ML_PID:-}" ]]; then
    kill "$ML_PID" 2>/dev/null || true
  fi

  if [[ -n "${FRONTEND_PID:-}" ]]; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

if [[ -f "$ROOT_DIR/.env.local" ]]; then
  set -a
  source "$ROOT_DIR/.env.local"
  set +a
fi

if [[ -z "${CFBD_API_KEY:-}" ]]; then
  echo "ERROR: CFBD_API_KEY is not set."
  echo
  echo "Either export it first:"
  echo '  export CFBD_API_KEY="your_key"'
  echo
  echo "or set it in:"
  echo "  $ROOT_DIR/.env.local"
  exit 1
fi

check_port() {
  local port="$1"
  local name="$2"

  if lsof -iTCP:"$port" -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "ERROR: $name port $port is already in use."
    echo "Run:"
    echo "  lsof -i :$port"
    echo "and stop the old process before running this script again."
    exit 1
  fi
}

check_port "$BACKEND_PORT" "Spring Boot"
check_port "$ML_PORT" "FastAPI"
check_port "$FRONTEND_PORT" "Vite"

echo "Starting PostgreSQL..."
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d

echo "Starting Spring Boot on port $BACKEND_PORT..."
(
  cd "$ROOT_DIR/backend"
  PORT="$BACKEND_PORT" mvn spring-boot:run
) &
BACKEND_PID=$!

echo "Starting FastAPI ML service on port $ML_PORT..."
(
  cd "$ROOT_DIR/ml"
  source .venv/bin/activate
  PORT="$ML_PORT" python inference_api.py
) &
ML_PID=$!

echo "Starting Vite frontend on port $FRONTEND_PORT..."
(
  cd "$ROOT_DIR/frontend"
  npm run dev -- --port "$FRONTEND_PORT" --strictPort
) &
FRONTEND_PID=$!

echo
echo "FourthDown AI is starting."
echo
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Backend:  http://localhost:$BACKEND_PORT"
echo "ML:       http://localhost:$ML_PORT"
echo "Postgres: localhost:5432"
echo
echo "Press Ctrl+C to stop Spring, FastAPI, and Vite."
echo "Postgres will remain running in Docker."

wait
