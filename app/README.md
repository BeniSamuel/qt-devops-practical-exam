# MyDiary

A personal diary app with JWT login and two roles. It is the application you deploy in the
**DevOps Engineer Practical Exam — Challenge B**.

| Part     | Technology                          | Folder      |
|----------|-------------------------------------|-------------|
| Frontend | Next.js 14 (standalone server)      | `frontend/` |
| Backend  | Spring Boot 3.5, Java 21, Maven     | `backend/`  |
| Database | PostgreSQL 16                       | —           |
| Cache    | Redis 7+ (optional)                 | —           |

> You get the source code only. Dockerfiles, `docker-compose.yml`, NGINX config and Kubernetes manifests are **your** job.

## What the app does

- Users register, log in and write diary entries with a mood, tags and a date.
- **USER** sees only their own diary. **ADMIN** manages accounts but cannot read anyone's entries.
- The dashboard shows statistics such as entry counts, writing streaks and moods.

## Test accounts

The first time the API starts on an empty database, it creates these accounts and some sample entries
automatically. Later restarts do not add anything.

| Role  | Email       | Password  |
|-------|-------------|-----------|
| ADMIN | admin@qt.rw | Admin@123 |
| USER  | demo@qt.rw  | Demo@123  |
| USER  | jane@qt.rw  | Jane@123  |

Log in as `demo` and `jane`: each one only sees their own entries. Set `SEED_ENABLED=false` outside the exam so no test accounts are created.

## Configuration

Everything is configured with environment variables. Copy the example files and edit them:
[backend/.env.example](backend/.env.example), [frontend/.env.example](frontend/.env.example).

For local runs the API also reads a `backend/.env` file. Real environment variables, such as Compose
`environment:` or a Kubernetes ConfigMap or Secret, override it. Never commit `.env`.

### Backend

| Variable | Default | Notes |
|---|---|---|
| `JWT_SECRET` | — | **Required**, at least 32 characters (`openssl rand -hex 32`) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/diary` | The database must already exist |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `diary` / `diary` | |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / — | Cache (optional) |
| `PORT` | `8080` | |
| `SEED_ENABLED` | `true` | |

### Frontend

| Variable | When | Notes |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | **build time** | Leave empty when NGINX serves the app and `/api` on the same address |
| `HOSTNAME` | runtime | Use `0.0.0.0` inside a container |

## Things to know when you deploy

- **Health check:** `GET /api/health` returns `200` when PostgreSQL is up (`503` otherwise) and reports
  `"cache": "up" | "down" | "disabled"`. It is reachable through NGINX like the rest of the API.
- **Kubernetes probes:** `GET /actuator/health/liveness` and `GET /actuator/health/readiness` (database only).
- **Redis is optional.** If it is down, the app keeps working and reads from PostgreSQL.
  `/api/health` then reports `"cache": "down"`, while readiness stays `UP`.
- **Database tables** are created automatically at startup (Flyway). Only the database itself must exist.
- **Next.js standalone build:** run `node server.js` from `.next/standalone/`. You must copy `.next/static/`
  and `public/` into it yourself, otherwise CSS and JavaScript return 404.
- `NEXT_PUBLIC_API_URL` is fixed when you run `next build`. Changing it later needs a rebuild.

## Build and run locally

Requirements: Java 21, Node.js 18.17+, PostgreSQL 16, Redis (optional).

```bash
# backend → http://localhost:8080
cd backend
cp .env.example .env          # set JWT_SECRET and the database settings
./mvnw spring-boot:run        # or: ./mvnw package && java -jar target/diary-api.jar

# frontend → http://localhost:3000
cd frontend
npm install
API_PROXY_TARGET=http://localhost:8080 npm run dev
npm run build                 # production build into .next/standalone
```

## API

All paths start with `/api`. Send `Authorization: Bearer <token>` except for health, register and login.

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | `/auth/register` · `/auth/login` | everyone | Get a token |
| GET | `/health` | everyone | Health check (database and cache) |
| GET | `/auth/me` | logged in | Current user |
| GET | `/entries` | logged in | Own entries (filters: `q`, `mood`, `tag`, `from`, `to`, `page`, `size`) |
| GET | `/entries/stats` | logged in | Dashboard statistics |
| GET · POST · PUT · DELETE | `/entries`, `/entries/{id}` | owner | Read and manage own entries |
| GET · PATCH · DELETE | `/admin/users`, `/admin/users/{id}/role`, `/admin/users/{id}` | ADMIN | Manage accounts |

## Project layout

```
backend/   Spring Boot API: controller/ service/ repository/ entity/ payload/ security/ config/
frontend/  Next.js app: app/ (pages), components/, lib/api.js
```
