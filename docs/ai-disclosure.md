# Khai báo sử dụng công cụ AI – BT2

Sinh viên: Võ Nguyễn Thành Nhân – 2374802010360. Dự án: Smart CRM – Mekong Mobile, L8. Khai báo BT1 gốc vẫn giữ tại [ai-disclosure.md](../ai-disclosure.md); tài liệu này bổ sung phần BT2, không thay hoặc suy diễn bằng chứng BT1.

| Công cụ | Dùng vào việc gì | Tệp/hàm áp dụng | Cách kiểm chứng và giới hạn |
|---|---|---|---|
| Codex | Kiểm kê source và đối chiếu UC/US/FR với rubric từ đề BT2 | `docs/bt2-gap-analysis.md`, `docs/checkpoint-0.md` | Đọc SRS/API/Drawio, văn bản DOCX BT1/BT2, xem wireframe, kiểm branch/diff; nguồn HoanThien chưa được cung cấp thì ghi rõ |
| Codex | Hoàn thiện gửi SMS/Zalo giả lập, transaction phản hồi, scheduler/retry/reminder | `SurveyDeliveryService`, `SurveyResponseService`, `SurveyScheduler`, controllers/repositories, migration V4/V5 | Kiểm thử unit/integration và bằng chứng Maven tại báo cáo; không tự coi source là runtime pass; V1/V2 đối chiếu checksum |
| Codex | Hỗ trợ login JWT/BCrypt, scope dữ liệu, aggregate/filter/XLSX | `auth/`, `reporting/`, migration V3 | Tests role/scope/filter/rounding, XLSX và MySQL thật theo kết quả suite; chưa đo NFR tải thì giữ NOT_VERIFIED |
| Codex | Hiện thực React theo wireframe và các màn phụ | `frontend/src/`, `frontend/tests/`, API client/Vite config | Unit giao diện, build và E2E; phân biệt mock API với luồng dùng backend/MySQL thật |
| Codex | Thực hiện CRUD có điều kiện cho lời mời và CRUD mẫu khảo sát; đo NFR01/02/03/05/06 | `SurveyInvitationService`, `SurveyTemplateService`, migration V6, `SurveyFlowIntegrationTest`, các load integration tests | 54 backend tests PASS; đo trên MySQL/servlet cục bộ và gateway giả lập. S1 vẫn chờ xác nhận học thuật; NFR07 chưa có người tham gia thật |
| Codex | Tài liệu chạy lại, môi trường, nghiệm thu, khai báo và báo cáo | `README.md`, `.env.example`, `docs/`, `scripts/test.ps1` | So biến với properties/source; clone sạch trên cùng máy đã qua test/build/E2E; chưa kiểm chứng trên máy khác |

Phần giao cho AI vẫn cần sinh viên đọc, giải thích quy tắc/TX/SQL/phân quyền và tự xác nhận trước khi nộp. Không có bằng chứng thử nghiệm người dùng 5 người thì không ghi đã đạt NFR07. Không dùng AI để giả ảnh kiểm thử hoặc hợp thức hóa sửa/xóa phản hồi trái BT1.

## Khai báo bổ sung cho BT3

**[GIẢ ĐỊNH – cần đối chiếu đề BT3]:** Bốn cột dưới đây là đề xuất vì chưa có biểu mẫu bốn cột chính thức trong đề được cung cấp.

| Công cụ AI | Mục đích sử dụng | Phần việc/tệp được hỗ trợ | Cách kiểm chứng và giới hạn |
|---|---|---|---|
| Codex (phiên làm việc này; giao diện không cung cấp model ID) | Khảo sát Git/source; hỗ trợ đóng gói Docker Compose và health endpoint; hỗ trợ thay đổi claim JWT; soạn/cập nhật test plan/report, bug log, deployment/API/security docs và báo cáo BT3 | `Dockerfile`, `docker-compose.yml`, `.env.example`, `.dockerignore`, `.gitattributes`, `HealthController`, auth classes/tests, `frontend/nginx.conf`, `docs/` | Backend suite cục bộ 57/57 PASS; frontend 18/18, build, Playwright 12/12 PASS; npm audit production 0 vulnerabilities; các probe health/injection/scope đã chạy. Docker Compose build và health đã xác minh trên bản sao cục bộ sạch từ bundle, không phải clone GitHub/tag; riêng test profile Compose thất bại (ReportingIntegrationTest 4/6 fail) và clone theo tag chưa chạy. Không dùng AI tạo kết quả/ảnh giả; người nộp cần rà soát và xác nhận trước khi nộp. |

Các test, số liệu và trạng thái trong BT3 chỉ được ghi từ log/lệnh quan sát được. Nội dung tạo bằng AI cần được sinh viên tự kiểm tra, bổ sung ảnh thật và sửa nếu không khớp lần chạy phát hành.
