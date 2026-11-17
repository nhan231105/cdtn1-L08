# Smart CRM L8 Deployment Guide

## 1. Requirements

- Windows with Docker Desktop, macOS with Docker Desktop, or Linux with Docker Engine 24 or later.
- Docker Compose V2.20 or later. Check with `docker compose version`.
- Git and an Internet connection for the first image and dependency downloads.
- At least 4 GB of free disk space for the images and MySQL volume.
- TCP port 3000 must be available. MySQL and the backend have no published host ports.
- The public demo values in `.env.example` are for a local classroom run only. Replace all sample keys and passwords, disable account bootstrap, and put the system behind a TLS reverse proxy before any real deployment.
- For a real HTTPS deployment, terminate TLS at the reverse proxy, proxy both the UI and `/api`, preserve `X-Forwarded-Proto`, and set Spring Boot `server.forward-headers-strategy=framework` so generated links use the external scheme. Keep the UI and API on the same origin; CORS is not an authentication control.

## 2. Setup steps

1. Clone the release tag:

   ```sh
   git clone --branch v1.0 https://github.com/nhan231105/cdtn1-L08.git
   cd cdtn1-L08
   ```

2. Create the local environment file:

   ```sh
   cp .env.example .env
   ```

3. Build and start the stack:

   ```sh
   docker compose up -d --build
   ```

Flyway applies V1–V6 when the API starts. V2 and V6 create synthetic sample data; no separate migration or seed command is required. The API starts after MySQL reports healthy, and the frontend starts after the API health check passes.

## 3. Environment variables

| Name | Meaning | Sample value | Required |
|---|---|---|---|
| `DB_NAME` | MySQL application database | `smartcrm_l8` | Yes |
| `DB_USER` | API database account, created by the MySQL image | `smartcrm` | Yes |
| `DB_PASSWORD` | Local sample password for the API database account | `smartcrm_local_demo_password` | Yes |
| `MYSQL_ROOT_PASSWORD` | Local root password used by MySQL initialization and the isolated test profile | `local_demo_root_password` | Yes |
| `APP_PORT` | Host port for the React UI and proxied API | `3000` | No; defaults to `3000` |
| `SERVICE_API_KEY` | Key for internal invitation endpoints | `local-demo-service-key-0123456789abcdef` | Yes |
| `JWT_SECRET` | HMAC key for access tokens | `local-demo-jwt-secret-0123456789abcdef` | Yes |
| `BOOTSTRAP_ENABLED` | Creates missing demo users at startup | `true` | Yes |
| `BOOTSTRAP_PASSWORD` | Sample password assigned to demo users on first creation | `DemoPasswordOnly123` | Yes |
| `SURVEY_PUBLIC_BASE_URL` | Base URL used to build public survey links | `http://localhost:3000/khao-sat` | Yes |
| `GATEWAY_SIMULATE_FAILURE` | Makes the simulated message gateway fail when enabled | `false` | Yes |
| `SURVEY_EXPIRY_DAYS` | Survey link validity period in days | `7` | Yes |
| `SURVEY_SCHEDULER_ENABLED` | Enables invitation/retry/reminder polling | `true` | Yes |

`DB_URL` is set by Compose to `jdbc:mysql://db:3306/<DB_NAME>`; `DB_HOST=db` is also supplied to the API container. The API and database are private to the Compose network.

## 4. Sample data

The seed rows come from Flyway V2, V3, and V6, and the demo accounts come from `DemoAccountBootstrap`. A first startup creates 2 service centers, 2 customers, 2 technicians, 4 tickets, 4 device labels, 1 active survey template, and 4 demo users. The sample phone numbers and tickets are fictional.

Demo users are `manager101`, `manager102`, `marketing`, and `director`. Their initial password is the `.env` value of `BOOTSTRAP_PASSWORD`. Bootstrap only creates missing accounts; it does not reset a password or overwrite an existing user.

## 5. Verify it works

Run the commands from the repository root.

```sh
docker compose ps
```

Expected: `db`, `api`, and `app` show `healthy`.

```sh
curl --fail http://localhost:3000/health
```

Expected response: `{"status":"ok","db":"up"}`.

```sh
docker compose --profile testing run --rm test
```

Expected: Maven reports `BUILD SUCCESS`, with zero failures, errors, and skipped tests. The test profile uses the separate `smartcrm_l8_test` database created by `db/init/001-create-test-schema.sql`.

**Release status (2026-11-17):** the local Compose test profile is currently failing. `ReportingIntegrationTest` reproduced 4 failures out of 6, including a 7-hour timestamp mismatch. Do not treat this guide as a verified v1.0 release until the failures are fixed and the full command above passes.

To repeat the JWT-claim assertion (the claim check in TC-06), run this single test:

```sh
docker compose --profile testing run --rm test ./mvnw -B -Dtest=AuthServiceTest test
```

Expected: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

For a local run without Compose, rerun that same single assertion from the repository root.

PowerShell:

```powershell
Push-Location backend
.\mvnw.cmd -B -Dtest=AuthServiceTest test
Pop-Location
```

Linux/macOS:

```sh
cd backend && ./mvnw -B -Dtest=AuthServiceTest test
```

To verify repeat startup, run `docker compose restart api`, then run `docker compose ps` and the health request again. Flyway should report that no migration is necessary. Seed counts should remain `2|2|2|4|4|1|4` for service centers, customers, technicians, tickets, devices, survey templates, and app users.

### Troubleshooting

- If port 3000 is in use, set a different `APP_PORT` in `.env`, then run `docker compose up -d` again.
- If an old classroom database volume is blocking a clean first-run check, `docker compose down -v` removes this Compose project's MySQL data. This permanently deletes the demo database in that volume; do not use it for data you need to keep.
- For startup details, run `docker compose logs api --tail 50` and `docker compose logs db --tail 50`. Do not share `.env` or logs that contain local credentials.
