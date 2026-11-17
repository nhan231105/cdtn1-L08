# Báo cáo hiện trạng BT3 — Smart CRM L8

Ngày khảo sát/cập nhật: 2026-11-17. Repository công khai suy ra từ remote Git: `https://github.com/nhan231105/cdtn1-05`. Track **SE**, luồng **L8 – CSAT sau bảo hành**. Tên/MSSV trong tài liệu được suy ra từ hồ sơ BT2 đã commit: **Võ Nguyễn Thành Nhân – 2374802010360**; cần sinh viên xác nhận trước khi xuất PDF.

Đề BT3 chính thức và tài liệu buổi 13 không có trong workspace. Các tên cột Test Report, bốn phần Test Plan, bảy trường Bug Log và bốn cột khai báo AI đang theo prompt, có nhãn giả định trong từng tài liệu.

## C1–C9

| Tiêu chí | Trạng thái | Bằng chứng và giới hạn |
|---|---|---|
| C1 — Dựng từ clone sạch bằng tối đa hai lệnh sau khi clone | ❌ Chưa nghiệm thu | `docker compose --env-file .env.example config --quiet` và cấu hình profile `testing` đều exit 0. Nhưng Docker Desktop trả `Docker Desktop is unable to start`; `docker info` không kết nối được daemon. Chưa build image, chạy container, hoặc clone tag `v1.0`. |
| C2 — Test Report 10–12 case, 8 cột, 5 loại | ✅ Hoàn tất có giả định | `docs/test-report.md` có 12 case, 8 cột và đủ loại luồng đúng/biên/dữ liệu sai/bảo mật/lặp lại. Kết quả được ghi từ backend, frontend và Playwright chạy thật ngày 17/11. Tên cột cần đối chiếu đề chính thức. |
| C3 — Một lệnh chạy test và chạy lại một case | ⚠️ Một phần | `powershell -NoProfile -File scripts/test.ps1` chạy 57 backend tests, không failure/error/skip. Lệnh riêng `.\mvnw.cmd -B -Dtest=AuthServiceTest test` chạy lại một test claim JWT (phần của TC-06), 1/1 PASS. Câu lệnh rerun Compose chờ Docker daemon để xác minh. 18 frontend tests và hai lượt Playwright cũng PASS nhưng là các lệnh riêng. |
| C4 — Ít nhất 3 bug, có 1 Critical/Major | ❌ Chưa đạt | Có 3 bug thật đã sửa: hai Low ở test harness và một Medium về claim JWT; không có Critical/Major đã được chứng minh. Không nâng mức giả tạo. Các liên kết commit mới chỉ truy cập công khai sau khi được push. |
| C5 — Guide đủ 5 mục và Verify it works | ⚠️ Chưa nghiệm thu | Năm mục cùng lệnh/kết quả mong đợi có trong `docs/deployment-guide.md`; không thể xác nhận output container khi daemon không chạy. |
| C6 — Checklist 7 mục có minh chứng | ⚠️ Một phần | `docs/checklist-bao-mat.md` có đủ 7 mục. Mục 1–4 có kết quả scan/test; mục 5–7 ghi CHƯA LÀM, trạng thái hiện có và kế hoạch. Chưa có ảnh `sec-1.png`–`sec-7.png`; mục log container và Maven SCA chưa chạy. |
| C7 — Không có secret thật; `.env` chưa vào history | ✅ Đạt ở lần rà soát | `git check-ignore -v .env` xác nhận `.gitignore` chặn file; `git log --all --oneline -- .env` rỗng. Không đọc `.env`. `.env.example` chỉ chứa credential giả chạy local và hướng dẫn thay khóa. Pattern scan trước đó chỉ thấy cấu hình web client Firebase trong tài liệu bên thứ ba và mật khẩu test, không thấy credential Smart CRM. |
| C8 — Chạy lần hai không lỗi/nhân đôi dữ liệu | ⚠️ Một phần | Backend test thật kiểm tra Flyway và số dòng seed không đổi khi chạy lại cùng datadir (`2|2|2|4|1|4`). Compose restart và đếm lại trên volume mới chưa chạy. |
| C9 — Tài liệu track đúng hệ thống | ⚠️ Rà soát gần xong | Source có 23 method routes, `docs/api-docs.md` có đúng 23. Route/schema đối chiếu controller/DTO; success samples dẫn đến test thật, có token đã che. Cần đối chiếu biểu mẫu đề BT3 và hoàn tất mẫu runtime/container sau khi Compose chạy. |

## Tình trạng triển khai và bảo mật theo SE

- Stack thật: Java 21 / Spring Boot 4.1.2, React/Vite / Node 24, MySQL 8.0.46. Compose tách `db`, `api`, `app` (Nginx UI); CSDL/backend không publish port host.
- JWT hiện dùng HS256 với `verify`, khóa từ env tối thiểu 32 byte, hạn 3.600 giây; payload có `sub`, `role`, `exp`. `AuthServiceTest` và suite backend PASS.
- Manager center 101 gọi báo cáo center 102 bị 403; SQL-shaped `center_id` trả 400. Response API/Excel che điện thoại. BCrypt được xác nhận trong test MySQL.
- CORS/cookie không áp dụng với demo cùng origin và bearer header; Guide ghi cách đặt TLS proxy khi triển khai thật.
- `npm audit --omit=dev`: 0 lỗ hổng production frontend. Maven dependency scan chưa chạy. Lỗi JSON và DB-off/log container chưa được kiểm tra trên Compose.
- Compose cấu hình đã parse thành công với `.env.example`, cả profile mặc định và `testing`. Docker CLI có cài, nhưng Docker Desktop daemon không lên; không có phép thử máy trắng.

## Ba việc còn thiếu nặng nhất

1. **C1/C8:** Khởi động được Docker daemon, build/run stack bằng sample env, kiểm health/test, chạy lần hai và clone thử release tag từ thư mục sạch.
2. **C4/C3:** Tìm và sửa thêm bug thật nếu có để đạt Major/Critical; không gán mức độ cao cho lỗi không tương xứng. Xác minh lệnh chạy một test case riêng trong container.
3. **C5–C9/nộp:** Chạy Maven SCA và các probe log/DB-off; chụp ảnh Compose/sec; xin đề BT3 chính thức hoặc giữ nhãn giả định; push commit/tag ở đúng chặng; ghép một PDF, xuất đúng tên rồi nộp VLU E-learning.

## Chưa thực hiện phát hành

BT3 hiện là thay đổi local trên `feat/bt2-setup`; chưa push, chưa tạo tag `v1.0`, chưa có release URL hoặc PDF BT3. Chưa có yêu cầu xác nhận giảng viên đã chuyển sang chặng 4. Không ghi nhận deploy cộng điểm.
