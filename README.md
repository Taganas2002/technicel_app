# Technician Management System

Internal app for Admin and Technician drop-list tracking.

**Stack:** Java 17, Spring Boot 3, Maven, MySQL 8, React + TypeScript + Vite.

This repository is built in phases. Steps 11–15 added the technician and admin UI. Step 16 is the end-to-end acceptance review.

## Project structure

```
backend/     Spring Boot API
frontend/    React SPA
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20+
- Docker (for local MySQL), or a local MySQL 8 instance

Production (Oracle VM + Cloudflare/Netlify) is documented in [DEPLOY.md](DEPLOY.md). Do that after local secrets and Docker files are in place. Do **not** use Oracle Autonomous Database — this app is MySQL.

## 1. Start MySQL

```bash
docker compose up -d
```

This starts MySQL on port `3306` with database `technicel`.

Copy environment defaults if you want a local `.env`:

```bash
copy .env.example .env
```

## 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API listens on [http://localhost:8080](http://localhost:8080).

Health check (no auth): [http://localhost:8080/api/health](http://localhost:8080/api/health)

Login:

```bash
POST http://localhost:8080/api/auth/login
{"nip":"ADMIN001","password":"admin123"}
```

Local seed admin (created on first backend start): NIP `ADMIN001`, password `admin123`.

Send the token on later requests:

```
Authorization: Bearer <accessToken>
```

Connection settings come from environment variables (with local defaults):

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` or `prod` |
| `SERVER_PORT` | `8080` | API port |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `technicel` | Database name |
| `DB_USERNAME` | `technicel` | Database user |
| `DB_PASSWORD` | `technicel` | Database password |
| `JWT_SECRET` | local placeholder | Must be unique and 32+ characters in prod |
| `ADMIN_NIP` | `ADMIN001` | Seed admin, used from Step 3 |
| `ADMIN_PASSWORD` | `admin123` | Must be changed in prod |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated frontend URLs |

`prod` profile requires `DB_HOST`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. It also refuses to start if `JWT_SECRET` or `ADMIN_PASSWORD` are still the local defaults.

The backend will not fully start until MySQL is reachable. Flyway creates the schema on startup (`V1__init_schema.sql`).

## 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI listens on [http://localhost:5173](http://localhost:5173). Vite proxies `/api` to `http://localhost:8080`.

Optional: copy `frontend/.env.example` to `frontend/.env` to set `VITE_API_BASE_URL`.

## Domain model

```
User 1──* WorkList 1──* DailyProgress
User 1──* DailyProgress
User 1──* DayOffRequest (as technician)
User 1──* DayOffRequest (as approver)
```

- `users`: unique `technician_code`, unique `nip`, role `ADMIN|TECHNICIAN`
- `work_lists`: OBD/DPO, at most one `IN_PROGRESS` list per technician
- `daily_progress`: one row per list per day; totals are calculated, not stored
- `day_off_requests`: `PENDING|APPROVED|REJECTED`

## Assumptions (Step 4)

- Java package is `com.technicel`.
- Admin and technicians both log in with NIP + password.
- JWT Bearer tokens are used. Inactive users cannot log in, and existing tokens stop working once the account is deactivated.
- Technician ID ownership is enforced in the backend. Changing an ID in the URL cannot access another technician.
- `/api/admin/**` is ADMIN only. Only Admin can create, update, or deactivate technicians.
- Password hashes are never returned in API responses.
- Created technicians always have role `TECHNICIAN`.
- Technicians create their own work lists. They cannot change `initialDropCount` after creation.
- Only Admin can PATCH `/api/admin/lists/{id}/initial-drops`, which recalculates remaining/progress/status.
- Daily progress is stored as one row per list per day. Same-day posts update that day's entry.
- A list becomes `FINISHED` when completed drops reach the initial count.
- Technicians can request days off. Only Admin can approve or reject. Already processed requests cannot be decided again.
