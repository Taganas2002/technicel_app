#!/bin/bash
set -euo pipefail
cd "$HOME/technicel_app"

if ! command -v docker >/dev/null 2>&1; then
  sudo dnf install -y yum-utils
  sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  sudo systemctl enable --now docker
  sudo usermod -aG docker opc || true
fi

sudo firewall-cmd --permanent --add-port=8080/tcp || true
sudo firewall-cmd --reload || true

if [ ! -f .env ]; then
  JWT_SECRET="$(openssl rand -base64 48 | tr -d '\n')"
  DB_PASSWORD="$(openssl rand -base64 18 | tr -d '\n')"
  DB_ROOT_PASSWORD="$(openssl rand -base64 18 | tr -d '\n')"
  ADMIN_PASSWORD="$(openssl rand -base64 12 | tr -d '\n')"
  cat > .env <<EOF
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_NAME=technicel
DB_USERNAME=technicel
DB_PASSWORD=${DB_PASSWORD}
DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION_MS=86400000
ADMIN_NIP=ADMIN001
ADMIN_PASSWORD=${ADMIN_PASSWORD}
ADMIN_FIRST_NAME=System
ADMIN_LAST_NAME=Admin
ADMIN_TECHNICIAN_CODE=ADMIN
CORS_ALLOWED_ORIGINS=http://151.145.32.173:8080
JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m
EOF
fi

echo "ADMIN_LOGIN"
grep -E 'ADMIN_NIP|ADMIN_PASSWORD' .env

sudo docker compose -f docker-compose.prod.prebuilt.yml --env-file .env up -d
sudo docker compose -f docker-compose.prod.prebuilt.yml ps
