#!/bin/bash
set -euo pipefail
cd "$HOME/technicel_app"

if [ ! -f /swapfile2 ]; then
  sudo fallocate -l 2G /swapfile2 || sudo dd if=/dev/zero of=/swapfile2 bs=1M count=2048
  sudo chmod 600 /swapfile2
  sudo mkswap /swapfile2
  sudo swapon /swapfile2
  grep -q '/swapfile2' /etc/fstab || echo '/swapfile2 none swap sw 0 0' | sudo tee -a /etc/fstab
fi
sudo swapon --show || true
free -h

if [ ! -x "$HOME/jre/bin/java" ]; then
  echo "Downloading JRE..."
  curl -fsSL -o /tmp/jre.tar.gz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jre/hotspot/normal/eclipse"
  mkdir -p "$HOME/jre"
  tar -xzf /tmp/jre.tar.gz -C "$HOME/jre" --strip-components=1
  rm -f /tmp/jre.tar.gz
fi
"$HOME/jre/bin/java" -version

if [ ! -x /usr/local/bin/dockerd ]; then
  echo "Downloading static Docker..."
  curl -fsSL -o /tmp/docker.tgz "https://download.docker.com/linux/static/stable/x86_64/docker-27.5.1.tgz"
  sudo tar -C /usr/local -xzf /tmp/docker.tgz
  sudo ln -sfn /usr/local/docker/docker /usr/local/bin/docker
  sudo ln -sfn /usr/local/docker/dockerd /usr/local/bin/dockerd
  sudo ln -sfn /usr/local/docker/containerd /usr/local/bin/containerd
  sudo ln -sfn /usr/local/docker/runc /usr/local/bin/runc
  rm -f /tmp/docker.tgz
fi

if [ ! -x /usr/local/bin/docker-compose ]; then
  echo "Downloading docker compose..."
  curl -fsSL -o /tmp/docker-compose "https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-x86_64"
  sudo mv /tmp/docker-compose /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
fi

if ! pgrep -x dockerd >/dev/null; then
  sudo mkdir -p /var/lib/docker /var/run
  sudo nohup /usr/local/bin/dockerd >/tmp/dockerd.log 2>&1 &
  for i in $(seq 1 30); do
    if sudo docker info >/dev/null 2>&1; then break; fi
    sleep 2
  done
fi
sudo docker info >/dev/null

if [ ! -f .env ]; then
  JWT_SECRET="$(openssl rand -base64 48 | tr -d '\n')"
  DB_PASSWORD="$(openssl rand -base64 18 | tr -d '\n')"
  DB_ROOT_PASSWORD="$(openssl rand -base64 18 | tr -d '\n')"
  ADMIN_PASSWORD="$(openssl rand -base64 12 | tr -d '\n')"
  cat > .env <<EOF
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_HOST=127.0.0.1
DB_PORT=3306
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
JAVA_TOOL_OPTIONS=-Xms96m -Xmx192m
EOF
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

echo "ADMIN_LOGIN"
echo "ADMIN_NIP=$ADMIN_NIP"
echo "ADMIN_PASSWORD=$ADMIN_PASSWORD"

sudo firewall-cmd --permanent --add-port=8080/tcp || true
sudo firewall-cmd --reload || true

sudo docker-compose -f docker-compose.mysql.yml --env-file .env up -d
echo "Waiting for MySQL..."
for i in $(seq 1 60); do
  if sudo docker exec technicel-mysql mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; then
    echo "MySQL is up"
    break
  fi
  sleep 5
done

pkill -f 'app.jar' || true
nohup env \
  SPRING_PROFILES_ACTIVE=prod \
  SERVER_PORT=8080 \
  DB_HOST=127.0.0.1 \
  DB_PORT=3306 \
  DB_NAME="$DB_NAME" \
  DB_USERNAME="$DB_USERNAME" \
  DB_PASSWORD="$DB_PASSWORD" \
  DB_USE_SSL=false \
  JWT_SECRET="$JWT_SECRET" \
  JWT_EXPIRATION_MS=86400000 \
  ADMIN_NIP="$ADMIN_NIP" \
  ADMIN_PASSWORD="$ADMIN_PASSWORD" \
  ADMIN_FIRST_NAME="$ADMIN_FIRST_NAME" \
  ADMIN_LAST_NAME="$ADMIN_LAST_NAME" \
  ADMIN_TECHNICIAN_CODE="$ADMIN_TECHNICIAN_CODE" \
  CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
  JAVA_TOOL_OPTIONS="-Xms96m -Xmx192m" \
  "$HOME/jre/bin/java" -jar "$HOME/technicel_app/backend/app.jar" \
  >/tmp/technicel-api.log 2>&1 &

sleep 8
tail -n 30 /tmp/technicel-api.log || true
echo LIGHT_START_DONE
