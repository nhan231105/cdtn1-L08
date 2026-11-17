# Báo cáo hiện trạng BT3 — Smart CRM L8

Ngày khảo sát/cập nhật: 2026-11-17. Repository công khai sinh viên xác nhận đã đổi tên: `https://github.com/nhan231105/cdtn1-L08`. Track **SE**, luồng **L8 – CSAT sau bảo hành**. Tên/MSSV trong tài liệu được suy ra từ hồ sơ BT2 đã commit: **Võ Nguyễn Thành Nhân – 2374802010360**; cần sinh viên xác nhận trước khi xuất PDF.

Đề BT3 chính thức và tài liệu buổi 13 không có trong workspace. Các tên cột Test Report, bốn phần Test Plan, bảy trường Bug Log và bốn cột khai báo AI đang theo prompt, có nhãn giả định trong từng tài liệu.

## C1–C9

| Tiêu chí | Trạng thái | Bằng chứng và giới hạn |
|---|---|---|
| C1 — Dựng từ clone sạch bằng tối đa hai lệnh sau khi clone | ⚠️ Một phần | Từ bundle của commit local, bản sao sạch đã `docker compose up -d --build` thành công; `db`, `api`, `app` đều `healthy`, `/health` trả `{"status":"ok","db":"up"}`. Đây chưa phải clone tag `v1.0` từ GitHub; tag chưa có. |
| C2 — Test Report 10–12 case, 8 cột, 5 loại | ✅ Hoàn tất có giả định | `docs/test-report.md` có 12 case, 8 cột và đủ loại luồng đúng/biên/dữ liệu sai/bảo mật/lặp lại. Kết quả được ghi từ backend, frontend và Playwright chạy thật ngày 17/11. Tên cột cần đối chiếu đề chính thức. |
| C3 — Một lệnh chạy test và chạy lại một case | ❌ Chưa đạt trên Compose | Các kết quả local trước đó vẫn là 57 backend, 18 frontend và Playwright 12/12 PASS. Trong Compose, chạy lại riêng `ReportingIntegrationTest`: **4/6 fail**. Full profile cũng ghi nhận lỗi test nhắc giờ, tải và reporting; xem `docs/test-report.md`. |
| C4 — Ít nhất 3 bug, có 1 Critical/Major | ❌ Chưa đạt | Có 3 bug thật đã sửa: hai Low ở test harness và một Medium về claim JWT. Bộ test Compose hiện phát hiện lỗi thời gian lệch 7 giờ và lỗi reporting; chưa được sửa/commit hoặc đánh mức độ, vì đã sang chặng 4 và không được sửa code. |
| C5 — Guide đủ 5 mục và Verify it works | ⚠️ Chưa phát hành | Guide có đủ năm mục; Compose/health đã chạy thật trên bản sao bundle cục bộ. Hướng dẫn đang trỏ tag `v1.0` chưa được tạo; chưa nghiệm thu clone theo tag. |
| C6 — Checklist 7 mục có minh chứng | ⚠️ Một phần | Checklist ghi đủ 7 mục. Mục 1–6 có kết quả kiểm tra; Maven SCA mục 7 chưa hoàn tất. Ảnh yêu cầu `sec-1.png`–`sec-7.png` và ảnh Compose vẫn do sinh viên chụp, chưa có trong repo. |
| C7 — Không có secret thật; `.env` chưa vào history | ✅ Đạt ở lần rà soát | `git check-ignore -v .env` xác nhận `.gitignore` chặn file; `git log --all --oneline -- .env` rỗng. Không đọc `.env`. `.env.example` chỉ chứa credential giả chạy local và hướng dẫn thay khóa. Pattern scan trước đó chỉ thấy cấu hình web client Firebase trong tài liệu bên thứ ba và mật khẩu test, không thấy credential Smart CRM. |
| C8 — Chạy lần hai không lỗi/nhân đôi dữ liệu | ✅ Đạt trên volume kiểm thử | Sau restart API trong Compose, Flyway ghi `No migration necessary`; số dòng trước/sau không đổi: `2|2|2|4|4|1|4` (center/customer/technician/ticket/device/template/user). |
| C9 — Tài liệu track đúng hệ thống | ⚠️ Chưa đối chiếu đề | Mã có 23 route, API docs có 23; test/build/health examples có bằng chứng. Đề BT3 và tài liệu buổi 13 chưa được cung cấp; API docs cần kiểm lại sau khi sửa lỗi timezone/reporting. |

## Tình trạng triển khai và bảo mật theo SE

- Stack thật: Java 21 / Spring Boot 4.1.2, React/Vite / Node 24, MySQL 8.0.46. Compose tách `db`, `api`, `app` (Nginx UI); CSDL/backend không publish port host.
- JWT hiện dùng HS256 với `verify`, khóa từ env tối thiểu 32 byte, hạn 3.600 giây; payload có `sub`, `role`, `exp`. `AuthServiceTest` và suite backend PASS.
- Manager center 101 gọi báo cáo center 102 bị 403; SQL-shaped `center_id` trả 400. Response API/Excel che điện thoại. BCrypt được xác nhận trong test MySQL.
- CORS/cookie không áp dụng với demo cùng origin và bearer header; Guide ghi cách đặt TLS proxy khi triển khai thật.
- `npm audit --omit=dev`: 0 lỗ hổng production frontend. OWASP Dependency-Check Maven tải NVD đến 50.000/426.378 (12%) rồi bị dừng; không có kết quả audit backend.
- Compose build thật thành công trên bundle clone local; ba service healthy, `/health` PASS. JSON hỏng trả 400 chung; DB-off trả 503 `{"status":"error","db":"down"}`; scan 1.002 dòng log API/app có 0 match nhạy cảm.
- Compose test profile **không đạt**: `ReportingIntegrationTest` tái chạy 4/6 FAIL; lần full profile ghi nhận timestamp lệch 7 giờ, benchmark 500/500 gửi nhưng 0 trong 5 phút theo phép đo test, và lỗi reporting. Chưa có phép thử clone/tag GitHub.

## Ba việc còn thiếu nặng nhất

1. **Release blocker:** xử lý lỗi lệch giờ 7 tiếng và reporting test fail; cần được cho phép quay lại chặng sửa code rồi chạy lại test/white-box từ đầu.
2. **Bằng chứng còn thiếu:** hoàn tất Maven SCA; sinh viên chụp ảnh thật Compose, health, test và sec-1…sec-7. Đề BT3 chính thức/tài liệu buổi 13 chưa có.
3. **Nộp:** xác nhận tên/MSSV, ghép một PDF duy nhất, push tag `v1.0` sau khi suite đạt và kiểm clone/tag trong repo đã đổi tên, rồi nộp VLU. Hiện chưa push/tag để tránh phát hành bản có test fail.

## Chưa thực hiện phát hành

Sinh viên đã xác nhận bắt đầu chặng 4. Local `origin` đã được đổi sang `https://github.com/nhan231105/cdtn1-L08.git` và remote refs đã kiểm tra được; main hiện ở `86d472a`, nhánh feature trên GitHub ở `be82d2a`, tag mới nhất là `v0.2`. BT3 commit `ebf92c3` và các commit trước vẫn chỉ ở local `feat/bt2-setup`; chưa push, chưa tạo tag `v1.0`, chưa có PDF BT3. Chưa ghi nhận deploy cộng điểm.
