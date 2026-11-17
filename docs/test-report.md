# Test Report — Smart CRM L8

**[GIẢ ĐỊNH – cần đối chiếu đề BT3]:** Chưa có đề BT3 chính thức trong workspace; tám tên cột và nhãn loại test dưới đây theo prompt được cung cấp.

**Thời điểm chạy:** Backend suite 2026-11-17; Playwright system API cases TC-01–TC-11 và browser E2E TC-12 cùng ngày. TC-05 injection query được probe trực tiếp; TC-06 và TC-11 cũng kiểm tra lại trong system suite.

**Môi trường:** Java 21.0.12.1, Spring Boot 4.1.2, MySQL 8.0.46 test riêng cổng 3307, Node 24.19.0, Vitest 5.1.0, Playwright 1.64.1. Docker CLI/Compose có cài nhưng Docker Desktop không khởi động được, daemon không sẵn sàng; kết quả dưới đây chưa phải phép thử máy trắng bằng Compose.

| Mã | Loại | Tiền điều kiện | Bước chạy | Dữ liệu | Kết quả mong đợi | Kết quả thực tế | Trạng thái |
|---|---|---|---|---|---|---|---|
| TC-01 | Luồng đúng | DB sạch; Marketing; template seed | Tạo, xem, sửa, xóa template; đọc lại | `TC01-*` | 201/200/200/204/404 | POST 201; GET 200; PUT 200; DELETE 204; GET sau xóa 404. Ảnh [TC-01](screenshots/TC-01.png). | PASS — 17/11 |
| TC-02 | Biên | Marketing; template seed | Gửi độ dài tối đa, rồi vượt 1 ký tự | name 80/81; title 120; intro 1000 | Max được nhận; name 81 bị 400 | 80/120/1000 được nhận; name 81 trả 400 nêu trường `name`. Ảnh [TC-02](screenshots/TC-02.png). | PASS — 17/11 |
| TC-03 | Luồng đúng | Ticket 401 đóng, chưa có lời mời; service key | Tạo lời mời, đọc list/detail, đổi kênh trước gửi | `ticket_id=401`, `ZALO` | 201 `CHO_GUI`; read/update 200 | POST 201; GET list/detail 200; PATCH 200, channel `ZALO`. Ảnh [TC-03](screenshots/TC-03.png). | PASS — 17/11 |
| TC-04 | Dữ liệu sai | Marketing đăng nhập | Tạo template thiếu `name`, so số dòng | Thiếu name | 400, chỉ rõ trường, không thêm dòng | 400 `VALIDATION_FAILED`; field `name`; số template 1→1. Ảnh [TC-04](screenshots/TC-04.png). | PASS — 17/11 |
| TC-05 | Dữ liệu sai | Ticket 404 đang mở; Marketing | Tạo lời mời sai; gửi SQL injection vào `center_id` | Thiếu `ticket_id`; `ticket_id=404`; `' OR '1'='1` | 400/422/400; không lộ SQL/stack | Missing id 400; ticket mở 422; template lạ 404, không lộ stack/SQL/JDBC. Injection query trả 400 `VALIDATION_FAILED`. Ảnh [TC-05](screenshots/TC-05.png). | PASS — 17/11 |
| TC-06 | Bảo mật | Marketing và manager101 | Manager gọi API Marketing; đọc center khác; JWT thiếu/sai; kiểm claims | manager center 101, request center 102 | 403; JWT thiếu/sai 401; claims tối thiểu | Role mismatch 403; center 102 trả 403 `FORBIDDEN_SCOPE`; JWT thiếu/sai 401; payload keys `exp,role,sub`; template không đổi. Ảnh [TC-06](screenshots/TC-06.png). | PASS — 17/11 |
| TC-07 | Bảo mật | API lời mời | GET thiếu/sai/đúng service key; POST không key | Header `X-Service-Key` | 401/401/200; POST bị chặn | GET 401/401/200; POST 401; số lời mời ticket 403 không đổi. Ảnh [TC-07](screenshots/TC-07.png). | PASS — 17/11 |
| TC-08 | Lặp lại | Ticket 402/403 đóng, chưa có lời mời | Hủy lời mời chưa gửi; tạo lại; thử sửa/xóa sau gửi | Ticket 402/403 | Hủy xong không phát hành lại; lời mời đã gửi bất biến | 402: create/delete/reissue 201/204/409; 403: create/send/PATCH/DELETE 201/200/409/409. Ảnh [TC-08](screenshots/TC-08.png). | PASS — 17/11 |
| TC-09 | Lặp lại | Cùng datadir; chạy app hai lần | Kiểm log startup và Flyway history | V1–V6 | Lần hai no-op; sáu migration thành công | Log lần hai ghi `No migration necessary`; history 6/6, phiên bản 1–6. Ảnh [TC-09](screenshots/TC-09.png). | PASS — 17/11 |
| TC-10 | Lặp lại | Cùng datadir; lưu count trước | Khởi động lại; so count và định dạng password hash | centers/customers/technicians/tickets/templates/users | Count không đổi; password bcrypt | Count trước/sau `2|2|2|4|1|4`; hash account Marketing có dạng BCrypt. Ảnh [TC-10](screenshots/TC-10.png). | PASS — 17/11 |
| TC-11 | Dữ liệu sai | Ticket 401 có lời mời; Marketing; service key | Tạo trùng; gửi injection trong center_id; lưu chuỗi SQL-shaped trong name | Duplicate ticket; `' OR 1=1 -- <script>…` | Duplicate 409; query 400; chuỗi lưu nguyên văn như dữ liệu | Duplicate 409, count=1; injection query 400 `VALIDATION_FAILED`; POST/GET/DELETE chuỗi literal 201/200/204. Ảnh [TC-11](screenshots/TC-11.png). | PASS — 17/11 |
| TC-12 | Luồng đúng | Frontend và backend; ticket tổng hợp mới | Gửi phản hồi, kiểm chống trùng, scope, masked phone, export và XSS | score 5; comment `<script>…` | Một phản hồi bất biến; script không chạy; scope/export đúng | Playwright 1/1; comment được render an toàn; không dialog; API/report scope và Excel export đạt. Ảnh [TC-12](screenshots/TC-12.png). | PASS — 17/11 |

## Kết quả suite chạy thật

- `powershell -NoProfile -File scripts/test.ps1` — **57 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS**, 2026-11-17, MySQL test mới trên 3307.
- `Push-Location backend; .\mvnw.cmd -B -Dtest=AuthServiceTest test; Pop-Location` — **1 test, 0 failures/errors/skips; BUILD SUCCESS**, 2026-11-17. Đây là phép chạy lại độc lập phần claim JWT của TC-06.
- `npm test` — **18/18 tests PASS**, 2026-11-17.
- `npm run build` — Vite build thành công, 2026-11-17.
- `npm run test:e2e -- system-qa.spec.js` — **11/11 cases PASS**, 2026-11-17.
- `npm run test:e2e -- l8.spec.js` — **1/1 browser case PASS**, 2026-11-17.
- `npm audit --omit=dev` — **0 vulnerabilities**, frontend production dependencies, 2026-11-17.
- `docker compose --env-file .env.example config --quiet` and the same command with `--profile testing` — **exit 0**; Compose syntax and interpolation validated, images/containers were not built or started.
- `docker compose up -d --build`, container test profile và phép thử máy trắng — **CHƯA CHẠY**; `docker compose version` trả `v5.7.0`, nhưng `docker info` báo không kết nối được Docker API và Docker Desktop trả `Docker Desktop is unable to start`.

Không gộp những lần chạy khác ngày thành một lần chạy duy nhất. Các case TC-05/06/11 đã có probe trực tiếp mới nhưng system suite và ảnh của chúng cần chạy/chụp lại sau khi kiểm tra Compose.
