# Screenshot checklist for BT3

Take screenshots from the real terminal/application after running the listed command. Keep the command and result visible. Redact tokens, phone numbers, passwords, `.env` contents, and authorization headers before saving. Do not generate or edit a fake result image.

## Deployment evidence still missing

The Compose stack was built and its health checks were verified on a clean local bundle copy on 2026-11-17. This was not a public `v1.0` clone. No genuine terminal screenshots were captured for that run, and the Compose test profile failed; do not capture or label a test-suite PASS.

| File | Command/result visible |
|---|---|
| `docker-compose-ps.png` | `docker compose ps` with `db`, `api`, and `app` all `healthy` |
| `health.png` | `curl --fail http://localhost:3000/health` and `{"status":"ok","db":"up"}` |
| `tests.png` | Capture only after the full test profile actually succeeds; the 2026-11-17 Compose run failed and must not be presented as PASS |
| `c8-second-startup.png` | `docker compose restart api`, `docker compose ps`, Flyway `No migration necessary`, and unchanged seed counts |
| `white-machine-clone.png` | Clone command for tag `v1.0`, then the two setup commands from the Deployment Guide and successful Verify output |

## Security checklist evidence

| File | Command/probe and result visible |
|---|---|
| `sec-1.png` | `git check-ignore -v .env`; empty `git log --all --oneline -- .env`; secret scan summary |
| `sec-2.png` | Three injection/XSS/shell grep commands and SQL-shaped `center_id` probe returning 400 |
| `sec-3.png` | manager101 request for center 102 returning 403 |
| `sec-4.png` | MySQL query that reports hash format only as `BCRYPT`; never show the hash value |
| `sec-5.png` | Malformed JSON and stopped test DB requests; sanitized error bodies without stack/table/connection details |
| `sec-6.png` | `docker compose logs --no-color api app | grep -iE 'password|token|0[0-9]{9}'` with no matching output |
| `sec-7.png` | `npm audit --omit=dev` plus backend Maven dependency scan output and any remediation plan |

## Test case evidence

`TC-01.png`–`TC-12.png` are present and were regenerated from the Playwright system/browser run on 2026-11-17. Keep the initial harness failure images as separate historical evidence. `TC-06` and `TC-11` must show the updated scope/JWT-claim and SQL-injection checks.
