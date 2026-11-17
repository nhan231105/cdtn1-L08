# Báo cáo hiện trạng BT3 — Smart CRM L8

Ngày cập nhật cuối: 2026-11-17. Repository công khai sinh viên xác nhận đã đổi tên: `https://github.com/nhan231105/cdtn1-L08`. Track **SE**, luồng **L8 – CSAT sau bảo hành**. Tên/MSSV **Võ Nguyễn Thành Nhân – 2374802010360** được suy ra từ hồ sơ BT2 trong repo; sinh viên cần xác nhận trước khi đặt tên PDF.

Đề BT3 chính thức và tài liệu buổi 13 không có trong workspace. Các tên cột Test Report, bốn phần Test Plan, bảy trường Bug Log và bốn cột khai báo AI đang theo prompt, có nhãn giả định trong từng tài liệu.

## C1–C9

| Tiêu chí | Trạng thái | Bằng chứng và giới hạn |
|---|---|---|
| C1 — Dựng từ clone sạch bằng tối đa hai lệnh sau khi clone | ⚠️ Một phần | Từ bundle của commit local, bản sao sạch đã `docker compose up -d --build` thành công; `db`, `api`, `app` đều `healthy`, `/health` trả `{"status":"ok","db":"up"}`. Đây chưa phải clone tag `v1.0` từ GitHub. |
| C2 — Test Report 10–12 case, 8 cột, 5 loại | ⚠️ Đủ hình thức, chưa đạt kết quả toàn bộ | `docs/test-report.md` có 12 case, 8 cột và 5 loại theo prompt. Kết quả local cũ được chạy thật, nhưng kiểm tra mới trong Compose phát hiện test fail; kết quả không thể trình bày như một bộ PASS. Cấu trúc vẫn cần đối chiếu đề chính thức. |
| C3 — Một lệnh chạy test và chạy lại một case | ❌ Chưa đạt trên Compose | Các kết quả local trước đó vẫn là 57 backend, 18 frontend và Playwright 12/12 PASS. Trong Compose, chạy lại riêng `ReportingIntegrationTest`: **4/6 fail**. Full profile cũng ghi nhận lỗi test nhắc giờ, tải và reporting; xem `docs/test-report.md`. |
| C4 — Ít nhất 3 bug, có 1 Critical/Major | ❌ Chưa đạt | Có 3 bug thật đã sửa: hai Low ở test harness và một Medium về claim JWT. Bộ test Compose hiện phát hiện lỗi thời gian lệch 7 giờ và lỗi reporting; chưa được sửa/commit hoặc đánh mức độ, vì đã sang chặng 4 và không được sửa code. |
| C5 — Guide đủ 5 mục và Verify it works | ⚠️ Chưa nghiệm thu release | Guide có đủ năm mục và lệnh verify. Compose/health đã chạy thật trên bản sao bundle cục bộ. Clone theo tag GitHub chưa kiểm chứng; test profile hiện fail. |
| C6 — Checklist 7 mục có minh chứng | ⚠️ Một phần | Checklist ghi đủ 7 mục. Mục 1–6 có kết quả kiểm tra; Maven SCA mục 7 chưa hoàn tất. Ảnh yêu cầu `sec-1.png`–`sec-7.png` và ảnh Compose vẫn do sinh viên chụp, chưa có trong repo. |
| C7 — Không có secret thật; `.env` chưa vào history | ✅ Kiểm tra đã thực hiện | `git check-ignore -v .env` cho thấy `.gitignore` chặn file; `git log --all --oneline -- .env` rỗng. `.env` không được mở/đọc. Các giá trị mẫu trong `.env.example` là thông tin demo. Pattern scan trước đó không tìm thấy credential Smart CRM; các chuỗi nhận diện trong tài liệu bên thứ ba/test không được dùng làm secret ứng dụng. |
| C8 — Chạy lần hai không lỗi/nhân đôi dữ liệu | ✅ Đạt trên volume kiểm thử | Sau restart API trong Compose, Flyway ghi `No migration necessary`; số dòng trước/sau không đổi: `2|2|2|4|4|1|4` (center/customer/technician/ticket/device/template/user). |
| C9 — Tài liệu track đúng hệ thống | ⚠️ Đối chiếu mã đạt, nghiệm thu đề chưa xong | Source có 23 method routes; `docs/api-docs.md` có đúng 23, gồm method/path/auth/request/response/error/limits. Route inventory được đối chiếu controller/DTO. API success examples có nguồn từ lần gọi thật trước đây. Đề BT3/tài liệu buổi 13 chưa được cung cấp; test Compose hiện fail. |

## Tình trạng triển khai và bảo mật theo SE

- Stack thật: Java 21 / Spring Boot 4.1.2, React/Vite / Node 24, MySQL 8.0.46. Compose tách `db`, `api`, `app` (Nginx UI); CSDL/backend không publish port host.
- JWT hiện dùng HS256 với `verify`, khóa từ env tối thiểu 32 byte, hạn 3.600 giây; payload có `sub`, `role`, `exp`. `AuthServiceTest` và suite backend PASS.
- Manager center 101 gọi báo cáo center 102 bị 403; SQL-shaped `center_id` trả 400. Response API/Excel che điện thoại. BCrypt được xác nhận trong test MySQL.
- CORS/cookie không áp dụng với demo cùng origin và bearer header; Guide ghi cách đặt TLS proxy khi triển khai thật.
- `npm audit --omit=dev`: 0 lỗ hổng production frontend. OWASP Dependency-Check Maven tải NVD đến 50.000/426.378 (12%) rồi bị dừng; không có kết quả audit backend.
- Compose build thật thành công trên bundle clone local; ba service healthy, `/health` PASS. JSON hỏng trả 400 chung; DB-off trả 503 `{"status":"error","db":"down"}`; scan 1.002 dòng log API/app có 0 match nhạy cảm.
- Compose test profile **không đạt**: `ReportingIntegrationTest` tái chạy 4/6 FAIL; lần full profile ghi nhận timestamp lệch 7 giờ, benchmark 500/500 gửi nhưng 0 trong 5 phút theo phép đo test, và lỗi reporting. Chưa có phép thử clone/tag GitHub.

## Sáu thành phần BT3 và trạng thái tài liệu

| Thành phần | Tệp | Trạng thái cuối |
|---|---|---|
| TP1 — Test Plan | `docs/test-plan.md` | Có bản rút gọn theo prompt; cần đối chiếu đề BT3/tài liệu buổi 13 (chưa được cung cấp). |
| TP2 — Test Report | `docs/test-report.md` | Có 12 TC, 8 cột, 5 loại theo giả định; ghi cả kết quả cũ và test Compose fail mới. Không đạt điều kiện bộ test PASS. |
| TP3 — Bug Log | `docs/bug-log.md` | 3 bug đã sửa, mỗi bug 7 dòng; không có bug Critical/Major đã sửa được chứng minh. Các lỗi mới từ Compose chưa sửa, không được bịa thành bug đã đóng. |
| TP4 — Packaging | `Dockerfile`, `docker-compose.yml`, `.dockerignore`, `.env.example`, `.gitignore`, `.gitattributes`, `db/init/`, `README.md` | Có; `docker compose up -d --build` đã chạy trên bundle clone sạch và health PASS. Clone theo tag chưa chạy; test profile fail. README ghi hai lệnh setup. |
| TP5 — Deployment/Security | `docs/deployment-guide.md`, `docs/checklist-bao-mat.md` | Có 5 mục Guide và 7 mục checklist. Checklist 5–6 có minh chứng Compose; mục 7 còn CHƯA LÀM vì backend scan chưa hoàn tất. |
| TP6 — Technical docs [SE] | `docs/api-docs.md` | Có 23/23 route theo inventory source; đề BT3 chưa có để đối chiếu định dạng. |
| Kèm theo | `docs/ai-disclosure.md`, `docs/thay-doi-thiet-ke.md`, `docs/screenshots/` | AI disclosure và design changes có. Ảnh TC-01…TC-12 hiện có; ảnh triển khai/`sec-1.png`…`sec-7.png` chưa có. |

## Những việc còn thiếu nặng nhất

1. **Kết quả kiểm thử:** Compose full test chưa đạt; `ReportingIntegrationTest` tái chạy 4/6 FAIL. Full run đã ghi nhận timestamp lệch 7 tiếng, lỗi reporting và benchmark tải không đạt. Chặng cuối không sửa code; không ghi bộ test là PASS.
2. **Bảo mật/bằng chứng:** Maven Dependency-Check không hoàn tất; lần chạy dừng ở 50.000/426.378 bản ghi NVD. Chưa có ảnh do sinh viên chụp cho Compose và sec-1…sec-7. Không tạo ảnh giả.
3. **Hồ sơ nộp:** cần sinh viên xác nhận họ tên/MSSV, cung cấp đề BT3 chính thức/tài liệu buổi 13 nếu muốn bỏ các giả định; cần ghép/xuất một PDF duy nhất và tự nộp VLU. Không có công cụ truy cập VLU trong phiên này.

## Chưa thực hiện phát hành

Sinh viên đã xác nhận chặng cuối. `origin` trỏ `https://github.com/nhan231105/cdtn1-L08.git`; các thay đổi BT3 trên `feat/bt2-setup` được xây trên `origin/main`. Tag `v1.0` trỏ snapshot tài liệu/bài làm này theo yêu cầu phát hành; tag **không đồng nghĩa** các tiêu chí C1–C4 đã đạt: test Compose fail và clone theo tag chưa được thực hiện. Chưa có PDF BT3, link deploy cộng điểm hay xác nhận nộp VLU.

## Nhật ký kiểm chứng cuối chặng (2026-11-17)

- Docker Engine 29.9.3 / Compose 5.7.0: build từ local bundle clone thành công; `db`, `api`, `app` đều healthy; `/health` trả `{\"status\":\"ok\",\"db\":\"up\"}`.
- C8 trên volume test: trước/sau restart API `2|2|2|4|4|1|4`; Flyway ghi `No migration necessary`.
- DB-off: `/health` trả HTTP 503 và `{\"status\":\"error\",\"db\":\"down\"}`; JSON sai trả HTTP 400 `VALIDATION_FAILED`, không có stack/SQL/JDBC.
- Log API/app: quét 1.002 dòng với `password|token|0[0-9]{9}` có 0 match.
- Lệnh chạy lại được trong container: `AuthServiceTest` 1/1 PASS. Đây không thay thế full suite.
- `ReportingIntegrationTest`: 6 test, 4 failure, 0 error, 0 skipped. Full Compose profile cũng ghi nhận failure khác; xem chi tiết và ranh giới lần chạy trong `docs/test-report.md`.
- Maven Dependency-Check 12.1.0: tải dữ liệu NVD đến 50.000/426.378 (12%), không có báo cáo kết quả; mục checklist 7 vẫn CHƯA LÀM.
- `git check-ignore -v .env` chặn `.env`; `git log --all --oneline -- .env` không có kết quả. Không đọc `.env`.
- Clone nhánh/tag công khai mới và tạo PDF/nộp VLU: CHƯA THỰC HIỆN.
- File `.env` sẵn có tại workspace developer không được đóng gói; khi rà tên biến, thấy thiếu `DB_NAME` và `MYSQL_ROOT_PASSWORD`. Không đọc hay ghi lại giá trị. Bản dựng sạch đã tạo `.env` từ `.env.example`, gồm đủ biến và chạy được.
