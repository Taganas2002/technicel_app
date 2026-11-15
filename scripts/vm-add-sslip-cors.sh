#!/bin/bash
set -eu
cd /home/opc/technicel_app
ORIGINS=$(grep '^CORS_ALLOWED_ORIGINS=' .env | cut -d= -f2-)
NEW='http://151-145-32-173.sslip.io:8080'
if echo "$ORIGINS" | grep -q sslip.io; then
  echo CORS_ALREADY_HAS_SSLIP
else
  python3 - <<PY
from pathlib import Path
path = Path("/home/opc/technicel_app/.env")
text = path.read_text()
old = None
for line in text.splitlines():
    if line.startswith("CORS_ALLOWED_ORIGINS="):
        old = line
        break
if old is None:
    raise SystemExit("missing CORS_ALLOWED_ORIGINS")
new = old + ",http://151-145-32-173.sslip.io:8080"
path.write_text(text.replace(old, new, 1))
print("CORS_UPDATED")
PY
fi
grep '^CORS_ALLOWED_ORIGINS=' .env
bash /home/opc/technicel_app/vm-start-java.sh