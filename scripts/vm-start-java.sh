#!/bin/bash
set -euo pipefail
cd /home/opc/technicel_app
pkill -f 'app.jar' || true
DB_NAME=$(grep '^DB_NAME=' .env | cut -d= -f2-)
DB_USERNAME=$(grep '^DB_USERNAME=' .env | cut -d= -f2-)
DB_PASSWORD=$(grep '^DB_PASSWORD=' .env | cut -d= -f2-)
JWT_SECRET=$(grep '^JWT_SECRET=' .env | cut -d= -f2-)
ADMIN_NIP=$(grep '^ADMIN_NIP=' .env | cut -d= -f2-)
ADMIN_PASSWORD=$(grep '^ADMIN_PASSWORD=' .env | cut -d= -f2-)
ADMIN_FIRST_NAME=$(grep '^ADMIN_FIRST_NAME=' .env | cut -d= -f2-)
ADMIN_LAST_NAME=$(grep '^ADMIN_LAST_NAME=' .env | cut -d= -f2-)
ADMIN_TECHNICIAN_CODE=$(grep '^ADMIN_TECHNICIAN_CODE=' .env | cut -d= -f2-)
CORS_ALLOWED_ORIGINS=$(grep '^CORS_ALLOWED_ORIGINS=' .env | cut -d= -f2-)
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
echo JAVA_PID=$!
sleep 20
tail -n 40 /tmp/technicel-api.log
ss -lntp | grep 8080 || true
echo JAVA_START_DONE
