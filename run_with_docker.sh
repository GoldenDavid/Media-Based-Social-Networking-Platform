#!/bin/bash
set -euo pipefail

echo "==> Building application jars..."
./gradlew build -x test

echo "==> Building Docker images..."
docker compose build

echo "==> Starting services..."
docker compose up -d

echo "==> Done. Run 'docker compose logs -f' to follow output."
