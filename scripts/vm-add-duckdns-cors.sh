#!/bin/bash
set -eu
cd /home/opc/technicel_app
python3 - <<'PY'
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
extra = "http://droptech.duckdns.org:8080"
if extra in old:
    print("CORS_ALREADY_HAS_DUCKDNS")
else:
    path.write_text(text.replace(old, old + "," + extra, 1))
    print("CORS_UPDATED")
PY
grep '^CORS_ALLOWED_ORIGINS=' .env
bash /home/opc/technicel_app/vm-start-java.sh