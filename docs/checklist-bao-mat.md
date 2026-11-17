# Checklist bảo mật — Smart CRM L8

Ngày rà soát: 2026-11-17. “CHƯA LÀM” không được hiểu là đã đạt; các lệnh Compose cần chạy sau khi Docker daemon khởi động được.

## 1. Không có secret dự án trong repo/history — ĐÃ LÀM

**Minh chứng:** `git check-ignore -v .env` trả `.gitignore:15:.env`; `git log --all --oneline -- .env` không có dòng kết quả. Pattern scan code/history không tìm thấy credential của dự án. Scan có nhận diện một Firebase web client config trong tệp HTML bên thứ ba `docs/design-changes.md` và password literal chỉ dùng trong test; đây không phải secret thuộc Smart CRM. Không ghi giá trị vào tài liệu.

## 2. Kiểm tra dữ liệu đầu vào — ĐÃ LÀM

**Minh chứng:** `rg -n '(SELECT|INSERT|UPDATE|DELETE).*(\+ *[a-zA-Z_]|\$\{)' backend/src frontend/src` có hai kết quả: `ReportingRepository.java:173` ghép `where(filter)` là các mệnh đề cố định; giá trị lọc được bind bằng named parameter (`:start`, `:end`, `:scoreMin`, `:scoreMax`, `:center`, `:technician`). Kết quả thứ hai là test fixture `ReportingIntegrationTest.java:153` dùng `JdbcTemplate` với bind parameters. `rg -n 'innerHTML|dangerouslySetInnerHTML|v-html' backend/src frontend/src` trả 0; `rg -n 'exec\(|execSync|spawn\(|os\.system|shell=True' backend/src frontend/src` trả 0. Gửi `center_id=' OR '1'='1` đến `/api/survey-responses` ngày 17/11 trả **400 `VALIDATION_FAILED`** (TC-05/TC-11).

## 3. Phân quyền theo phạm vi sở hữu — ĐÃ LÀM

**Minh chứng:** manager101 thuộc center 101 gọi `/api/survey-responses?center_id=102` nhận **403 `FORBIDDEN_SCOPE`** ngày 17/11; logic kiểm tra tại `ReportingService.scoped` và case bảo mật TC-06. Khách hàng không có tài khoản đăng nhập; link khảo sát dùng token ngẫu nhiên một lần và phản hồi không thể sửa sau gửi.

## 4. Hash mật khẩu — ĐÃ LÀM

**Minh chứng:** TC-10 truy vấn hash account `marketing` trên MySQL test, kết quả nhận dạng **BCRYPT**; cách tạo hash ở `DemoAccountBootstrap` dùng `BCryptPasswordEncoder`. Không ghi hash thật vào ảnh/tài liệu.

## 5. Lỗi không lộ nội bộ — CHƯA LÀM

**Minh chứng hiện có:** TC-05 ngày 13/11 gửi dữ liệu sai và xác nhận response không có stack/SQL/JDBC; `HealthControllerTest` kiểm tra DB probe hỏng chỉ trả `{"status":"error","db":"down"}`. Chưa chạy request JSON lỗi và gọi API khi DB thực sự tắt trong container. **Kế hoạch:** khi Docker khả dụng, gửi JSON sai, dừng DB riêng của compose, gọi API/health và lưu ảnh `docs/screenshots/sec-5.png`.

## 6. Log không chứa secret/dữ liệu cá nhân — CHƯA LÀM

**Minh chứng hiện có:** `docker compose logs --no-color api app | Select-String -Pattern 'password|token|0[0-9]{9}'` chưa chạy: Docker CLI/Compose có cài, nhưng Docker Desktop báo `Docker Desktop is unable to start` và named pipe daemon không tồn tại. **Kế hoạch:** chạy sau khi daemon hoạt động, kiểm cả API lẫn Nginx, không lưu log thô nếu có dữ liệu; chụp kết quả rỗng vào `docs/screenshots/sec-6.png`.

## 7. Phiên bản dependency và kiểm lỗ hổng — CHƯA LÀM

**Minh chứng hiện có:** `npm audit --omit=dev` ngày 17/11 báo **0 vulnerabilities** cho production dependencies React; `frontend/package-lock.json` khóa dependency graph. Chưa chạy audit cho Maven/backend dependencies, nên mục chung chưa được đánh dấu hoàn tất. **Kế hoạch:** chạy công cụ SCA Maven phù hợp với dependency tree trước tag; ghi advisory và kế hoạch xử lý nếu có.

## Ghi chú theo track SE

- **JWT:** ĐÃ LÀM — secret từ biến môi trường dài tối thiểu 32 byte; HS256 được kiểm rõ ràng; TTL 3600 giây; payload test có đúng `sub`, `role`, `exp`; mỗi request tải lại user theo ID. Bằng chứng: `AuthServiceTest` và backend suite 57/57 PASS.
- **CORS:** KHÔNG ÁP DỤNG cho cấu hình demo — React và API dùng cùng origin qua Nginx; không cấu hình wildcard. Khi tách origin triển khai phải dùng allow-list.
- **Cookie:** KHÔNG ÁP DỤNG — frontend gửi bearer token trong `Authorization`, không lưu token/mật khẩu trên URL hoặc cookie.
- **HTTPS:** Hướng dẫn triển khai thật nằm trong `docs/deployment-guide.md`: TLS tại reverse proxy, preserve `X-Forwarded-Proto`, Spring `server.forward-headers-strategy=framework`. Chưa triển khai HTTPS public.
- **HTTP errors/logs:** TC-05 kiểm tra lỗi dữ liệu sai; chưa hoàn tất kiểm tra DB ngắt và log container (mục 5–6).

Ảnh minh chứng còn cần chụp thật: `sec-1.png`–`sec-4.png` (các lệnh/kết quả nêu trên), rồi `sec-5.png`–`sec-7.png` sau các kiểm tra còn thiếu. Che token, phone, password và `.env` trước khi chụp.
