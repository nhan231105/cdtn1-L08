# Smart CRM L8 API Reference

**[GIẢ ĐỊNH – cần đối chiếu đề BT3]:** Đề chính thức chưa được cung cấp; tài liệu theo prompt. Inventory trong mã có **23 route** (22 phương thức controller và `GET /health`). `@RequestMapping` cấp lớp là base path, không phải endpoint riêng. Tên route/schema đã đối chiếu controller và DTO.

## Common rules

- Base URL for the local Compose demo: `http://localhost:3000`. The Nginx frontend proxies `/api` to the private Spring Boot service. The backend listens on port 8080 inside the Compose network; MySQL has no published host port.
- JWT is sent as `Authorization: Bearer <access_token>`. Login tokens expire after 3600 seconds. The signed payload contains `sub` (numeric `user_id`), `role`, and `exp`; the server reloads the current account and center scope for each request.
- The internal invitation API requires `X-Service-Key`; public survey endpoints require the unguessable survey token in the path. Survey tokens and bearer tokens are credentials: all examples redact them.
- Dates use `yyyy-MM-dd`; local timestamps use the Asia/Ho_Chi_Minh offset. Error body shape is `{"error":{"code":"...","message":"...","fields":{}}}`.
- Real examples below were captured from HTTP calls to the local API backed by a disposable MySQL test database on 2026-11-17, plus the Playwright run. Bearer and survey tokens are redacted. IDs and phone values belong to synthetic fixtures.

## Route inventory

| # | Method | Path | Access |
|---:|---|---|---|
| 1 | GET | `/health` | Public |
| 2 | POST | `/api/auth/login` | Public |
| 3 | GET | `/api/auth/me` | Any valid JWT |
| 4 | POST | `/api/survey-invitations` | `X-Service-Key` |
| 5 | GET | `/api/survey-invitations` | `X-Service-Key` |
| 6 | GET | `/api/survey-invitations/{id}` | `X-Service-Key` |
| 7 | PATCH | `/api/survey-invitations/{id}` | `X-Service-Key` |
| 8 | DELETE | `/api/survey-invitations/{id}` | `X-Service-Key` |
| 9 | POST | `/api/survey-invitations/{id}/send` | `X-Service-Key` |
| 10 | GET | `/api/survey-templates` | Marketing JWT |
| 11 | GET | `/api/survey-templates/{id}` | Marketing JWT |
| 12 | POST | `/api/survey-templates` | Marketing JWT |
| 13 | PUT | `/api/survey-templates/{id}` | Marketing JWT |
| 14 | DELETE | `/api/survey-templates/{id}` | Marketing JWT |
| 15 | GET | `/api/surveys/{token}` | Survey token |
| 16 | POST | `/api/surveys/{token}/responses` | Survey token |
| 17 | GET | `/api/survey-stats/by-technician` | Center manager JWT |
| 18 | GET | `/api/survey-stats/by-center` | Director JWT |
| 19 | GET | `/api/survey-stats/response-rate` | Center manager JWT |
| 20 | GET | `/api/survey-responses` | Center manager or Marketing JWT |
| 21 | GET | `/api/survey-responses/export` | Marketing JWT |
| 22 | GET | `/api/reference/centers` | Any valid JWT |
| 23 | GET | `/api/reference/technicians` | Any valid JWT |

## Endpoint details

### 1. `GET /health`

1. **Method and path:** `GET /health`.
2. **Business purpose:** Confirms that the API can query its configured MySQL database.
3. **Authentication and roles:** Public health probe; it returns no business data.
4. **Parameters/body:** None.
5. **Observed success response:** `HTTP 200` — `{"status":"ok","db":"up"}` (called directly on the running API on 2026-11-17).
6. **Errors:** `503` with `{"status":"error","db":"down"}` when the DB probe fails; the DB exception is not returned.
7. **Limits/behavior:** Uses `SELECT 1`; Compose uses this route for API and frontend health checks.

### 2. `POST /api/auth/login`

1. **Method and path:** `POST /api/auth/login`.
2. **Business purpose:** Authenticates an internal user and issues a short-lived bearer token.
3. **Authentication and roles:** Public; valid username/password required.
4. **Request parameters/body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `username` | string | Yes | 1–100 characters |
   | `password` | string | Yes | Nonblank; ≤72 UTF-8 bytes |

   Example JSON: `{"username":"marketing","password":"<local demo password>"}`.
5. **Observed success response:** `HTTP 200` — `{"username":"marketing","role":"MARKETING","center_id":null,"access_token":"<redacted>","token_type":"Bearer","expires_in":3600}` from the 2026-11-17 test login. Token value removed before documentation.
6. **Errors:** `401 UNAUTHENTICATED` for missing, invalid, disabled, or incorrect credentials; malformed JSON returns `400 VALIDATION_FAILED`.
7. **Limits/behavior:** Passwords are checked against BCrypt hashes. Failed account lookup also performs a dummy BCrypt comparison. The token is HS256 and expires after one hour.

### 3. `GET /api/auth/me`

1. **Method and path:** `GET /api/auth/me`.
2. **Business purpose:** Returns the current authenticated user's role and center scope.
3. **Authentication and roles:** Any valid JWT.
4. **Parameters/body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `Authorization` | header string | Yes | `Bearer <access_token>` |

   No query or request body.
5. **Observed success response:** `HTTP 200` — `{"username":"marketing","role":"MARKETING","center_id":null}` (same profile returned by the real login run).
6. **Errors:** `401 UNAUTHENTICATED` for missing, invalid, expired, or disabled-user tokens.
7. **Limits/behavior:** User and role are reloaded from MySQL; client-supplied center scope is not trusted.

### 4. `POST /api/survey-invitations`

1. **Method and path:** `POST /api/survey-invitations`.
2. **Business purpose:** Creates a survey invitation for an eligible closed warranty ticket.
3. **Authentication and roles:** `X-Service-Key`; this is an internal module API.
4. **Request body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `ticket_id` | integer | Yes | Positive ID of a closed ticket |

   Example JSON: `{"ticket_id":401}`.
5. **Observed success response:** `HTTP 201` — `{"invitation_id":4,"ticket_id":405,"ticket_code":"BT3-LIVE-405","status":"CHO_GUI","survey_url":"http://127.0.0.1:5173/khao-sat/<redacted>","created_at":"2026-11-17T19:58:40+07:00","expires_at":"2026-11-24T19:58:40+07:00"}`. Token redacted; ticket 405 was inserted only in the disposable test database.
6. **Errors:** `401` missing/invalid key; `400 VALIDATION_FAILED`; `404 TICKET_NOT_FOUND`; `422 TICKET_NOT_CLOSED`; `409 INVITATION_ALREADY_EXISTS` or `INVITATION_CANCELLED`.
7. **Limits/behavior:** Only `DA_DONG` tickets qualify. One invitation can exist per ticket; a cancelled invitation cannot be reissued. URL token is a 43-character credential and must be redacted in logs/screenshots.

### 5. `GET /api/survey-invitations`

1. **Method and path:** `GET /api/survey-invitations`.
2. **Business purpose:** Lists internal invitations for the sending module.
3. **Authentication and roles:** `X-Service-Key`.
4. **Parameters/body:** No query or request body. `X-Service-Key` header is required by the internal API filter.
5. **Observed success response:** `HTTP 200` — one returned item: `[{"invitation_id":4,"ticket_id":405,"ticket_code":"BT3-LIVE-405","survey_url":"http://127.0.0.1:5173/khao-sat/<redacted>","status":"CHO_GUI","channel":"SMS","send_attempts":0,"created_at":"2026-11-17T19:58:40","sent_at":null,"expires_at":"2026-11-24T19:58:40","survey_title":"Trải nghiệm của bạn thế nào?","survey_intro":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile."}]`. Token redacted.
6. **Errors:** `401` missing/invalid key; database failure returns a generic service error.
7. **Limits/behavior:** Newest invitations are listed first. This endpoint is internal and must not be exposed to an unauthenticated browser client.

### 6. `GET /api/survey-invitations/{id}`

1. **Method and path:** `GET /api/survey-invitations/{id}`.
2. **Business purpose:** Reads one invitation and its survey link for internal delivery.
3. **Authentication and roles:** `X-Service-Key`.
4. **Path parameter:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | integer | Yes | Positive invitation ID |

   `X-Service-Key` header required; no body.
5. **Observed success response:** `HTTP 200` — `{"invitation_id":4,"ticket_id":405,"ticket_code":"BT3-LIVE-405","survey_url":"http://127.0.0.1:5173/khao-sat/<redacted>","status":"CHO_GUI","channel":"SMS","send_attempts":0,"created_at":"2026-11-17T19:58:40","sent_at":null,"expires_at":"2026-11-24T19:58:40","survey_title":"Trải nghiệm của bạn thế nào?","survey_intro":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile."}`. Token redacted.
6. **Errors:** `401` invalid key; `400 VALIDATION_FAILED` for nonpositive ID; `404 INVITATION_NOT_FOUND`.
7. **Limits/behavior:** The result includes a credential-bearing `survey_url`; do not publish the unredacted value.

### 7. `PATCH /api/survey-invitations/{id}`

1. **Method and path:** `PATCH /api/survey-invitations/{id}`.
2. **Business purpose:** Changes the delivery channel before the first send attempt.
3. **Authentication and roles:** `X-Service-Key`.
4. **Path/body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | path integer | Yes | Positive invitation ID |
   | `channel` | JSON string | Yes | `SMS` or `ZALO` |

   Example JSON: `{"channel":"ZALO"}`. `X-Service-Key` header required.
5. **Observed success response:** `HTTP 200` — `{"invitation_id":4,"ticket_id":405,"ticket_code":"BT3-LIVE-405","survey_url":"http://127.0.0.1:5173/khao-sat/<redacted>","status":"CHO_GUI","channel":"ZALO","send_attempts":0,"created_at":"2026-11-17T19:58:40","sent_at":null,"expires_at":"2026-11-24T19:58:40","survey_title":"Trải nghiệm của bạn thế nào?","survey_intro":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile."}`. Token redacted.
6. **Errors:** `401`; `400 VALIDATION_FAILED`; `404 INVITATION_NOT_FOUND`; `409 INVITATION_CLOSED` after delivery/attempt.
7. **Limits/behavior:** Only `SMS` and `ZALO` are accepted; sent invitations cannot be changed.

### 8. `DELETE /api/survey-invitations/{id}`

1. **Method and path:** `DELETE /api/survey-invitations/{id}`.
2. **Business purpose:** Deletes an invitation that has never been sent.
3. **Authentication and roles:** `X-Service-Key`.
4. **Path parameter:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | integer | Yes | Positive invitation ID |

   `X-Service-Key` header required; no body.
5. **Observed success response:** `HTTP 204 No Content`; TC-08 observed this for an unsent invitation.
6. **Errors:** `401`; `400 VALIDATION_FAILED`; `404 INVITATION_NOT_FOUND`; `409 INVITATION_CLOSED` once sending has started.
7. **Limits/behavior:** Writes a cancellation tombstone for the ticket, preventing a replacement invitation. Survey responses are never deleted.

### 9. `POST /api/survey-invitations/{id}/send`

1. **Method and path:** `POST /api/survey-invitations/{id}/send`.
2. **Business purpose:** Sends or retries the invitation through the simulated message gateway.
3. **Authentication and roles:** `X-Service-Key`.
4. **Path/body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | path integer | Yes | Positive invitation ID |
   | `channel` | JSON string | No | `SMS` or `ZALO`; defaults to `SMS` |

   `X-Service-Key` header required. Example JSON: `{"channel":"SMS"}`.
5. **Observed success response:** `HTTP 200` — `{"invitation_id":4,"status":"DA_GUI","channel":"SMS","sent_at":"2026-11-17T19:58:40.1919872+07:00","send_attempts":1}`. Empty body selected the documented default channel `SMS`.
6. **Errors:** `401`; `400 VALIDATION_FAILED`; `404 INVITATION_NOT_FOUND`; `409` closed/invalid invitation state; `502 SMS_GATEWAY_ERROR` for a simulated provider failure.
7. **Limits/behavior:** The demo gateway does not contact SMS/Zalo providers. Retry count is capped at three and attempts are rate-spaced by the scheduler.

### 10. `GET /api/survey-templates`

1. **Method and path:** `GET /api/survey-templates`.
2. **Business purpose:** Lists survey templates available to Marketing.
3. **Authentication and roles:** Marketing JWT only.
4. **Parameters/body:** None.
5. **Observed success response:** `HTTP 200` — `{"items":[{"template_id":1,"name":"CSAT sau bảo hành","title":"Trải nghiệm của bạn thế nào?","intro_text":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile.","is_active":true,"created_at":"2026-11-17 19:54:55","updated_at":"2026-11-17 19:54:55"}]}` from the live test API.
6. **Errors:** `401 UNAUTHENTICATED`; `403 FORBIDDEN_SCOPE` for other roles.
7. **Limits/behavior:** Only one template may be active. Invitation text is snapshotted when its invitation is created.

### 11. `GET /api/survey-templates/{id}`

1. **Method and path:** `GET /api/survey-templates/{id}`.
2. **Business purpose:** Reads one survey template.
3. **Authentication and roles:** Marketing JWT only.
4. **Path parameter:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | integer | Yes | Positive template ID |

   No body.
5. **Observed success response:** `HTTP 200` — `{"template_id":1,"name":"CSAT sau bảo hành","title":"Trải nghiệm của bạn thế nào?","intro_text":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile.","is_active":true,"created_at":"2026-11-17 19:54:55","updated_at":"2026-11-17 19:54:55"}` from the live test API.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED`; `404 TEMPLATE_NOT_FOUND`.
7. **Limits/behavior:** Active state is included as `is_active`; timestamps are generated by MySQL.

### 12. `POST /api/survey-templates`

1. **Method and path:** `POST /api/survey-templates`.
2. **Business purpose:** Creates a survey template.
3. **Authentication and roles:** Marketing JWT only.
4. **Request body:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `name` | string | Yes | Nonblank; 1–80 characters; unique |
   | `title` | string | Yes | Nonblank; 1–120 characters |
   | `intro_text` | string | Yes | Nonblank; 1–1,000 characters |
   | `is_active` | boolean | Yes | At most one active template |

   Example JSON: `{"name":"TC01-demo","title":"How was your service?","intro_text":"Please share your experience.","is_active":false}`.
5. **Observed success response:** `HTTP 201` — `{"template_id":3,"name":"BT3-live-api-docs","title":"How was your service?","intro_text":"Please share your experience.","is_active":false,"created_at":"2026-11-17 19:59:23","updated_at":"2026-11-17 19:59:23"}`. This temporary template was deleted after the sample was captured.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED`; `409 TEMPLATE_NAME_EXISTS` or `ACTIVE_TEMPLATE_REQUIRED`.
7. **Limits/behavior:** Unique name; at most one active template. Template deletion/update rules preserve an active template.

### 13. `PUT /api/survey-templates/{id}`

1. **Method and path:** `PUT /api/survey-templates/{id}`.
2. **Business purpose:** Replaces a template's editable fields.
3. **Authentication and roles:** Marketing JWT only.
4. **Path/body:** `id` is a required positive template ID (integer). Body uses the same four required fields and constraints as endpoint 12:

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `name` | string | Yes | Nonblank; 1–80 characters; unique |
   | `title` | string | Yes | Nonblank; 1–120 characters |
   | `intro_text` | string | Yes | Nonblank; 1–1,000 characters |
   | `is_active` | boolean | Yes | At most one active template |

   Example JSON: `{"name":"TC01-demo","title":"Updated service survey?","intro_text":"Please share your experience.","is_active":false}`.
5. **Observed success response:** `HTTP 200` — `{"template_id":3,"name":"BT3-live-api-docs","title":"Updated service survey?","intro_text":"Please share your experience.","is_active":false,"created_at":"2026-11-17 19:59:23","updated_at":"2026-11-17 19:59:23"}` from the live test API.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED`; `404 TEMPLATE_NOT_FOUND`; `409 TEMPLATE_NAME_EXISTS` or active-template constraint.
7. **Limits/behavior:** Existing invitations retain their snapshotted title/intro text.

### 14. `DELETE /api/survey-templates/{id}`

1. **Method and path:** `DELETE /api/survey-templates/{id}`.
2. **Business purpose:** Deletes an inactive template.
3. **Authentication and roles:** Marketing JWT only.
4. **Path parameter:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `id` | integer | Yes | Positive template ID |

   No body.
5. **Observed success response:** `HTTP 204 No Content`; TC-01 deleted an inactive test template and then observed 404 on read.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED`; `404 TEMPLATE_NOT_FOUND`; `409 ACTIVE_TEMPLATE_DELETE`.
7. **Limits/behavior:** The active template cannot be deleted.

### 15. `GET /api/surveys/{token}`

1. **Method and path:** `GET /api/surveys/{token}`.
2. **Business purpose:** Loads the form for one delivered invitation.
3. **Authentication and roles:** Possession of the 43-character survey token; no user login.
4. **Path parameter:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `token` | string | Yes | URL-safe; exactly 43 characters |

   No query or body.
5. **Observed success response:** `HTTP 200` — `{"ticket_code":"BT3-LIVE-405","center_name":"Trung tam Tan Binh - DEMO","device_name":"Thiết bị #501","closed_at":"2026-11-17T19:57:48+07:00","expires_at":"2026-11-24T19:58:40+07:00","survey_title":"Trải nghiệm của bạn thế nào?","survey_intro":"Hãy chia sẻ cảm nhận về dịch vụ bảo hành của Mekong Mobile.","score_min":1,"score_max":5,"comment_max_length":1000}`. Path token redacted.
6. **Errors:** `404 SURVEY_NOT_FOUND`; `409 ALREADY_RESPONDED`; `410 SURVEY_EXPIRED`; `429 RATE_LIMITED`.
7. **Limits/behavior:** Only sent invitations for closed tickets are available. The response is immutable once submitted.

### 16. `POST /api/surveys/{token}/responses`

1. **Method and path:** `POST /api/surveys/{token}/responses`.
2. **Business purpose:** Submits the customer's one survey response.
3. **Authentication and roles:** Possession of the survey token.
4. **Path/body:** `token` is a required 43-character URL-safe path value. Request body:

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `score` | integer | Yes | 1–5 |
   | `comment` | string | No | ≤1,000 characters |

   Example JSON: `{"score":5,"comment":"E2E XSS alert(1) intact"}`.
5. **Observed success response:** `HTTP 201` — `{"response_id":2,"ticket_code":"BT3-LIVE-405","score":5,"comment":"E2E XSS alert(1) intact","responded_at":"2026-11-17T19:59:23+07:00","message":"Cảm ơn bạn đã phản hồi"}`. The API removed `<script>` tags from the saved comment.
6. **Errors:** `400 VALIDATION_FAILED`; `404 SURVEY_NOT_FOUND`; `409 ALREADY_RESPONDED`; `410 SURVEY_EXPIRED`; `429 RATE_LIMITED`.
7. **Limits/behavior:** Transaction inserts the response and changes invitation status. A second submission is rejected; no update/delete API exists for submitted responses.

### 17. `GET /api/survey-stats/by-technician`

1. **Method and path:** `GET /api/survey-stats/by-technician`.
2. **Business purpose:** Returns invitation and CSAT summaries grouped by technician for one center.
3. **Authentication and roles:** Center manager JWT; results are restricted to that manager's center.
4. **Query parameters:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `center_id` | integer | Yes | Positive; must be within manager scope |
   | `from`, `to` | date | No | `yyyy-MM-dd`; ordered range ≤366 days; defaults to current month |
   | `technician_id` | integer | No | Positive |
   | `score_min`, `score_max` | integer | No | 1–5; defaults to 1 and 5; min ≤ max |

   Example request: `/api/survey-stats/by-technician?center_id=101&from=2026-11-01&to=2026-11-30`.
5. **Observed success response:** `HTTP 200` — `{"from":"2026-11-01","to":"2026-11-30","center_id":101,"center_name":"Trung tam Tan Binh - DEMO","items":[{"technician_id":301,"technician_name":"Kỹ thuật viên #301","invitations_sent":1,"responses":1,"csat_avg":5.00,"satisfied_rate":1.00}]}` from the live test API.
6. **Errors:** `401`; `403 FORBIDDEN_SCOPE`; `400 VALIDATION_FAILED` for invalid filters or a missing center; `404 CENTER_NOT_FOUND`.
7. **Limits/behavior:** Maximum date range is 366 days. Manager cannot request another center; TC-06 verified center 102 returns 403.

### 18. `GET /api/survey-stats/by-center`

1. **Method and path:** `GET /api/survey-stats/by-center`.
2. **Business purpose:** Summarizes monthly CSAT and score distribution by center.
3. **Authentication and roles:** Director JWT only.
4. **Query parameters:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `from`, `to` | date | No | `yyyy-MM-dd`; ordered range ≤366 days; defaults to current month |
   | `center_id` | integer | No | Positive ID |
   | `group_by` | string | No | Only `month`; defaults to `month` |

   Example request: `/api/survey-stats/by-center?from=2026-11-01&to=2026-11-30&group_by=month`.
5. **Observed success response:** `HTTP 200` — `{"from":"2026-11-01","to":"2026-11-30","group_by":"month","items":[{"center_name":"Trung tam Tan Binh - DEMO","center_id":101,"month":"2026-11","responses":1,"csat_avg":5.00,"satisfied_rate":1.00,"distribution":{"1":0,"2":0,"3":0,"4":0,"5":1}},{"center_name":"Trung tam Thu Duc - DEMO","center_id":102,"month":"2026-11","responses":1,"csat_avg":5.00,"satisfied_rate":1.00,"distribution":{"1":0,"2":0,"3":0,"4":0,"5":1}}]}` from the live test API.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED` for invalid period or unsupported group.
7. **Limits/behavior:** Empty months are represented with zero counts and null averages; the range is at most 366 days.

### 19. `GET /api/survey-stats/response-rate`

1. **Method and path:** `GET /api/survey-stats/response-rate`.
2. **Business purpose:** Reports monthly response rate for invitations successfully sent in the selected cohort.
3. **Authentication and roles:** Center manager JWT, limited to the manager's center.
4. **Query parameters:** Same optional filter fields as endpoint 17 (`center_id`, `from`, `to`, `technician_id`, `score_min`, `score_max`), with the same types and limits. Example request: `/api/survey-stats/response-rate?center_id=101&from=2026-11-01&to=2026-11-30`.
5. **Observed success response:** `HTTP 200` — `{"items":[{"center_id":101,"month":"2026-11","invitations_sent":1,"responses":1,"response_rate":1.00}]}` from the live test API.
6. **Errors:** `401`; `403 FORBIDDEN_SCOPE`; `400 VALIDATION_FAILED`; `404 CENTER_NOT_FOUND`.
7. **Limits/behavior:** Cohort is based on successful send month; only responses received before the report end are counted. Maximum date range is 366 days.

### 20. `GET /api/survey-responses`

1. **Method and path:** `GET /api/survey-responses`.
2. **Business purpose:** Searches and paginates survey responses.
3. **Authentication and roles:** Center manager (own center only) or Marketing.
4. **Query parameters:**

   | Name | Type | Required | Constraint |
   |---|---|---|---|
   | `center_id`, `technician_id` | integer | No | Positive IDs; manager is scoped to own center |
   | `from`, `to` | date | No | `yyyy-MM-dd`; ordered range ≤366 days; defaults to current month |
   | `score_min`, `score_max` | integer | No | 1–5; defaults to 1 and 5; min ≤ max |
   | `page` | integer | No | 1-based; defaults to 1 |
   | `size` | integer | No | 1–100; defaults to 20 |

   Example request: `/api/survey-responses?center_id=101&page=1&size=20`.
5. **Observed success response:** `HTTP 200` — `{"total":1,"size":20,"items":[{"response_id":2,"ticket_code":"BT3-LIVE-405","center_name":"Trung tam Tan Binh - DEMO","technician_name":"Kỹ thuật viên #301","score":5,"comment":"E2E XSS alert(1) intact","customer_phone":"090****001","responded_at":"2026-11-17T19:59:23+07:00"}],"page":1}` from the live test API. Phone is masked.
6. **Errors:** `401`; `403 FORBIDDEN_SCOPE`; `400 VALIDATION_FAILED`; `404 CENTER_NOT_FOUND`.
7. **Limits/behavior:** Full phone numbers are not returned. Maximum page size is 100 and date range 366 days.

### 21. `GET /api/survey-responses/export`

1. **Method and path:** `GET /api/survey-responses/export`.
2. **Business purpose:** Exports the filtered response list as an XLSX workbook.
3. **Authentication and roles:** Marketing JWT only.
4. **Query parameters:** Same filter fields and constraints as endpoint 20, except `page` and `size` are not accepted. Example request: `/api/survey-responses/export?center_id=101&from=2026-11-01&to=2026-11-30`.
5. **Observed success response:** `HTTP 200`; live request returned an XLSX body of 1,957 bytes with content type `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`. The body is binary, not JSON.
6. **Errors:** `401`; `403`; `400 VALIDATION_FAILED`; `422 EXPORT_TOO_LARGE` when more than 10,000 rows match.
7. **Limits/behavior:** Export cap is 10,000 rows; phone values remain masked.

### 22. `GET /api/reference/centers`

1. **Method and path:** `GET /api/reference/centers`.
2. **Business purpose:** Supplies center options for authorized report filters.
3. **Authentication and roles:** Any valid JWT. Center managers see only their own center.
4. **Parameters/body:** None.
5. **Observed success response:** `HTTP 200` — `{"items":[{"center_name":"Trung tam Tan Binh - DEMO","center_id":101},{"center_name":"Trung tam Thu Duc - DEMO","center_id":102}]}` from the live test API.
6. **Errors:** `401 UNAUTHENTICATED`.
7. **Limits/behavior:** Returns IDs/labels only; no customer or ticket data.

### 23. `GET /api/reference/technicians`

1. **Method and path:** `GET /api/reference/technicians`.
2. **Business purpose:** Supplies technician options for report filters.
3. **Authentication and roles:** Any valid JWT. Manager output is scoped to their center; other roles are scoped according to report role rules.
4. **Query parameters:** Same optional filter fields and constraints as endpoint 17, except `center_id` is optional. Example request: `/api/reference/technicians?center_id=101`.
5. **Observed success response:** `HTTP 200` — `{"items":[{"center_id":101,"technician_name":"Kỹ thuật viên #301","technician_id":301}]}` from the live test API.
6. **Errors:** `401`; `403 FORBIDDEN_SCOPE`; `400 VALIDATION_FAILED`; `404 CENTER_NOT_FOUND`.
7. **Limits/behavior:** Missing technician display names use the explicit `Kỹ thuật viên #<id>` label; maximum filter period is 366 days.

## Error-code reference

| HTTP | Code | Meaning | Verified by |
|---:|---|---|---|
| 400 | `VALIDATION_FAILED` | Missing, malformed, or out-of-range input | TC-02, TC-04, TC-05, TC-11 |
| 401 | `UNAUTHENTICATED` | Missing/invalid JWT or service key | TC-06, TC-07 |
| 403 | `FORBIDDEN_SCOPE` | Role or center scope does not permit access | TC-06, TC-12 |
| 404 | `TICKET_NOT_FOUND`, `INVITATION_NOT_FOUND`, `TEMPLATE_NOT_FOUND`, `SURVEY_NOT_FOUND`, `CENTER_NOT_FOUND` | Resource is absent or survey token is invalid | TC-05; endpoint service tests |
| 409 | `INVITATION_ALREADY_EXISTS`, `INVITATION_CANCELLED`, `INVITATION_CLOSED`, `ALREADY_RESPONDED`, `TEMPLATE_NAME_EXISTS`, `ACTIVE_TEMPLATE_REQUIRED`, `ACTIVE_TEMPLATE_DELETE` | Duplicate or immutable state conflict | TC-01, TC-08, TC-11, TC-12 |
| 410 | `SURVEY_EXPIRED` | Survey link expired | Survey service tests |
| 422 | `TICKET_NOT_CLOSED`, `INVALID_PHONE`, `EXPORT_TOO_LARGE` | Business eligibility, invalid reference phone, or export-size rule | TC-05; delivery/export service tests |
| 429 | `RATE_LIMITED` | Survey request rate limit reached | `SurveyRateLimitFilterTest` |
| 502 | `SMS_GATEWAY_ERROR` | Simulated gateway send failed | `SurveyDeliveryService` integration tests |
| 503 | `SERVICE_UNAVAILABLE` | Database operation could not complete | `HealthControllerTest`; API-offline probe pending |

The table lists codes emitted by the code and tests; it does not imply that every error path was exercised in the same run. See `docs/test-report.md` for observed run boundaries.
