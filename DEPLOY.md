# Production deploy (Oracle VM + free frontend)

The app is MySQL + Spring Boot + a React build. Use an Oracle **Always Free Compute** VM for MySQL and the API. Put the frontend on Cloudflare Pages or Netlify.

Do **not** create Oracle Autonomous Database. This project is MySQL.

## 1. Prepare secrets locally

Copy `.env.production.example` to `.env` on the VM later. Generate strong values:

```bash
# Example (Git Bash / Linux)
openssl rand -base64 48
```

Required:

- `JWT_SECRET` — at least 32 characters, not the local placeholder
- `ADMIN_PASSWORD` — not `admin123`
- `DB_PASSWORD` and `DB_ROOT_PASSWORD`
- `CORS_ALLOWED_ORIGINS` — your live frontend URL, for example `https://technicel.pages.dev`

Prod startup **fails** if JWT or admin password are still the local defaults.

## 2. Oracle VM

1. Create Always Free Ampere (`VM.Standard.A1.Flex`) Ubuntu, or `VM.Standard.E2.1.Micro` if Ampere is full.
2. Open ports **22** and **8080** in the VCN security list and NSG. Do **not** open **3306**.
3. SSH in as `opc` (Oracle Linux) or `ubuntu`. For the 1 GB micro shape, run `scripts/oracle-linux-setup.sh` (adds swap, Docker, small Java/MySQL limits).
4. Copy this repo onto the VM.
5. Create `.env` from `.env.production.example`.
6. Start API + MySQL:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

7. Check `http://YOUR_VM_IP:8080/api/health`.

Local MySQL-only compose is unchanged: `docker compose up -d`.

## 3. Frontend (Cloudflare Pages or Netlify)

```bash
cd frontend
copy .env.production.example .env.production
```

Set `VITE_API_BASE_URL=http://YOUR_VM_IP:8080` (or `https://api.yourdomain.com` later).

```bash
npm install
npm run build
```

Upload the `frontend/dist` folder, or connect the Git repo:

- Build command: `npm run build`
- Publish directory: `dist`
- Root: `frontend`

SPA fallback is already in `frontend/public/_redirects` and `frontend/netlify.toml`.

After the frontend URL exists, add it to `CORS_ALLOWED_ORIGINS` on the VM and recreate the backend container:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## 4. Smoke test

- Open the frontend URL
- Log in as the new admin NIP/password
- Create a technician, create a list, save today's drops
- Confirm admin dashboard and Alerts

Change the seed admin password in `.env` **before** the first prod start. The seeder only creates the admin if that NIP does not exist yet.
