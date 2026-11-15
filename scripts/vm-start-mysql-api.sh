#!/bin/bash
set -euo pipefail
cd /home/opc/technicel_app
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
echo ADMIN_LOGIN
grep -E 'ADMIN_NIP|ADMIN_PASSWORD' .env
sudo firewall-cmd --permanent --add-port=8080/tcp || true
sudo firewall-cmd --reload || true
sudo env PATH=/usr/local/docker:/usr/local/bin:$PATH /usr/local/bin/docker-compose -f docker-compose.mysql.yml --env-file .env up -d
echo COMPOSE_UP
for i in $(seq 1 60); do
  if sudo /usr/local/bin/docker exec technicel-mysql mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; then
    echo MYSQL_UP
    break
  fi
  echo "waiting mysql $i"
  sleep 5
done
set -a
# shellcheck disable=SC1091
source .env
set +a
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
  /home/opc/jre/bin/java -jar /home/opc/technicel_app/backend/app.jar \
  >/tmp/technicel-api.log 2>&1 &
sleep 12
tail -n 40 /tmp/technicel-api.log || true
echo LIGHT_START_DONE
