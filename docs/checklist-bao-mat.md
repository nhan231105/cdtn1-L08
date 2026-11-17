# Checklist bảo mật — Smart CRM L8

Ngày rà soát: 2026-11-17. Phép thử Compose chạy trên bản sao cục bộ tạm; chưa phải clone tag GitHub `v1.0`.

## 1. Không có secret dự án trong repo/history — ĐÃ LÀM

**Minh chứng:** `git check-ignore -v .env` trả `.gitignore:15:.env`; `git log --all --oneline -- .env` không có dòng kết quả. Pattern scan code/history không tìm thấy credential của dự án. Scan có nhận diện một Firebase web client config trong tệp HTML bên thứ ba `docs/design-changes.md` và password literal chỉ dùng trong test; đây không phải secret thuộc Smart CRM. Không ghi giá trị vào tài liệu.

## 2. Kiểm tra dữ liệu đầu vào — ĐÃ LÀM

**Minh chứng:** `rg -n '(SELECT|INSERT|UPDATE|DELETE).*(\+ *[a-zA-Z_]|\$\{)' backend/src frontend/src` có hai kết quả: `ReportingRepository.java:173` ghép `where(filter)` là các mệnh đề cố định; giá trị lọc được bind bằng named parameter (`:start`, `:end`, `:scoreMin`, `:scoreMax`, `:center`, `:technician`). Kết quả thứ hai là test fixture `ReportingIntegrationTest.java:153` dùng `JdbcTemplate` với bind parameters. `rg -n 'innerHTML|dangerouslySetInnerHTML|v-html' backend/src frontend/src` trả 0; `rg -n 'exec\(|execSync|spawn\(|os\.system|shell=True' backend/src frontend/src` trả 0. Gửi `center_id=' OR '1'='1` đến `/api/survey-responses` ngày 17/11 trả **400 `VALIDATION_FAILED`** (TC-05/TC-11).

## 3. Phân quyền theo phạm vi sở hữu — ĐÃ LÀM

**Minh chứng:** manager101 thuộc center 101 gọi `/api/survey-responses?center_id=102` nhận **403 `FORBIDDEN_SCOPE`** ngày 17/11; logic kiểm tra tại `ReportingService.scoped` và case bảo mật TC-06. Khách hàng không có tài khoản đăng nhập; link khảo sát dùng token ngẫu nhiên một lần và phản hồi không thể sửa sau gửi.

## 4. Hash mật khẩu — ĐÃ LÀM

**Minh chứng:** TC-10 truy vấn hash account `marketing` trên MySQL test, kết quả nhận dạng **BCRYPT**; cách tạo hash ở `DemoAccountBootstrap` dùng `BCryptPasswordEncoder`. Không ghi hash thật vào ảnh/tài liệu.

## 5. Lỗi không lộ nội bộ — ĐÃ LÀM

**Minh chứng:** Trong Compose tạm, POST JSON sai cú pháp đến `/api/auth/login` trả **400** với `VALIDATION_FAILED`, thông báo chung, không có stack/SQL/JDBC. Khi dừng riêng `clone-db-1`, GET `/health` trả **503** với `{"status":"error","db":"down"}`; không có thông tin nội bộ trong body. DB test được khởi động lại và đạt `healthy`.

## 6. Log không chứa secret/dữ liệu cá nhân — ĐÃ LÀM

**Minh chứng:** quét log của hai container bằng `docker logs clone-api-1` và `docker logs clone-app-1`, mẫu `password|token|0[0-9]{9}`: **1.002 dòng, 0 kết quả khớp** sau các phép thử. Chỉ ghi số liệu, không đưa log thô vào tài liệu.

## 7. Phiên bản dependency và kiểm lỗ hổng — CHƯA LÀM

**Minh chứng:** `npm audit --omit=dev` ngày 17/11 báo **0 vulnerabilities** cho production dependencies React; `frontend/package-lock.json` khóa dependency graph. Thử chạy OWASP Dependency-Check Maven 12.1.0 nhưng lần cập nhật NVD bị dừng ở **50.000/426.378 bản ghi (12%)**; không sinh được báo cáo kết quả, nên backend chưa được kiểm lỗ hổng. **Kế hoạch:** hoàn tất scan trước phát hành; nếu cần API key, cung cấp riêng cho công cụ tại máy chạy và không commit/log giá trị.

## Ghi chú theo track SE

- **JWT:** ĐÃ LÀM — secret từ biến môi trường dài tối thiểu 32 byte; HS256 được kiểm rõ ràng; TTL 3600 giây; payload test có đúng `sub`, `role`, `exp`; mỗi request tải lại user theo ID. Bằng chứng: `AuthServiceTest` và backend suite 57/57 PASS.
- **CORS:** KHÔNG ÁP DỤNG cho cấu hình demo — React và API dùng cùng origin qua Nginx; không cấu hình wildcard. Khi tách origin triển khai phải dùng allow-list.
- **Cookie:** KHÔNG ÁP DỤNG — frontend gửi bearer token trong `Authorization`, không lưu token/mật khẩu trên URL hoặc cookie.
- **HTTPS:** Hướng dẫn triển khai thật nằm trong `docs/deployment-guide.md`: TLS tại reverse proxy, preserve `X-Forwarded-Proto`, Spring `server.forward-headers-strategy=framework`. Chưa triển khai HTTPS public.
- **HTTP errors/logs:** mục 5–6 đã kiểm tra trên Compose tạm như minh chứng trên; khi DB dừng, health probe trả lời chậm do chờ connection timeout trước khi trả 503.

Ảnh minh chứng người nộp còn cần chụp thật: `sec-1.png`–`sec-7.png` từ terminal của lần nghiệm thu; không có ảnh nào được tạo giả. Che token, phone, password và `.env` trước khi chụp.
