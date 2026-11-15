#!/bin/bash
set -euo pipefail
cd /home/opc/technicel_app
DB_NAME=$(grep '^DB_NAME=' .env | cut -d= -f2-)
DB_USERNAME=$(grep '^DB_USERNAME=' .env | cut -d= -f2-)
DB_PASSWORD=$(grep '^DB_PASSWORD=' .env | cut -d= -f2-)
if sudo -n /usr/local/bin/docker info >/dev/null 2>&1; then
  DOCKER="sudo -n /usr/local/bin/docker"
elif /usr/local/bin/docker info >/dev/null 2>&1; then
  DOCKER="/usr/local/bin/docker"
else
  echo DOCKER_UNAVAILABLE
  exit 1
fi
$DOCKER exec -e MYSQL_PWD="$DB_PASSWORD" technicel-mysql mysql --default-character-set=utf8mb4 -u"$DB_USERNAME" "$DB_NAME" -e "
DELETE FROM admin_notifications;
DELETE FROM daily_progress;
DELETE FROM day_off_requests;
DELETE FROM work_lists;
DELETE FROM users WHERE role = 'TECHNICIAN';
SELECT role, COUNT(*) AS accounts FROM users GROUP BY role;
SELECT COUNT(*) AS lists FROM work_lists;
SELECT COUNT(*) AS progress FROM daily_progress;
SELECT COUNT(*) AS day_off FROM day_off_requests;
SELECT COUNT(*) AS notes FROM admin_notifications;
"
echo RESET_DONE
