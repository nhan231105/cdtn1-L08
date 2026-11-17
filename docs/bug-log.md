# Bug Log — Smart CRM L8

**[GIẢ ĐỊNH – cần đối chiếu đề BT3]:** Mỗi bug có bảy dòng theo cấu trúc trong prompt. Chỉ ghi lỗi đã quan sát và sửa.

### QA-HARNESS-01 — Low

1. **Mã/mức độ:** QA-HARNESS-01 / Low — tạo test vector JWT.
2. **Phát hiện bởi:** TC-06, lần đầu 2026-11-13.
3. **Triệu chứng:** Token bị sửa nhưng test nhận 403 thay vì 401; ảnh lỗi [TC-06-initial-harness-failure](screenshots/TC-06-initial-harness-failure.png).
4. **Tái lập:** Chỉ đổi ký tự Base64URL cuối của chữ ký JWT rồi gọi endpoint Marketing.
5. **Nguyên nhân:** Ký tự cuối có thể chỉ đổi bit đệm không dùng, chữ ký giải mã vẫn giữ nguyên.
6. **Cách sửa:** Sửa ký tự Base64URL đầu tiên của chữ ký để thay đổi byte chữ ký.
7. **Commit sửa/chạy lại:** [2a973f4](https://github.com/nhan231105/cdtn1-L08/commit/2a973f4c1e2fbc35dad9db78c2e723554fa18b0e); TC-06 nhận 401 cho JWT giả và PASS.

### QA-HARNESS-02 — Low

1. **Mã/mức độ:** QA-HARNESS-02 / Low — giải mã log test.
2. **Phát hiện bởi:** TC-09, lần đầu 2026-11-13.
3. **Triệu chứng:** Test không tìm thấy `No migration necessary` và log hiển thị ký tự lỗi; ảnh [TC-09-initial-harness-failure](screenshots/TC-09-initial-harness-failure.png).
4. **Tái lập:** Đọc log PowerShell UTF-16LE như UTF-8 rồi so chuỗi Flyway.
5. **Nguyên nhân:** Harness không nhận biết encoding khi đọc log chuyển hướng.
6. **Cách sửa:** Detect byte NUL và đọc UTF-16LE; trường hợp khác đọc UTF-8.
7. **Commit sửa/chạy lại:** [b84aaca](https://github.com/nhan231105/cdtn1-L08/commit/b84aaca7cdf16107cbe03aab2a0036031405d49e); TC-09 tìm thấy thông báo và sáu migration PASS.

### BT3-SEC-01 — Medium

1. **Mã/mức độ:** BT3-SEC-01 / Medium — JWT mang claim thừa.
2. **Phát hiện bởi:** Rà soát checklist JWT và test claim ngày 2026-11-17.
3. **Triệu chứng:** JWT chứa username và center_id ngoài định danh, role và thời hạn cần thiết.
4. **Tái lập:** Đăng nhập, giải mã phần payload của JWT; kiểm tra các claim trước khi sửa.
5. **Nguyên nhân:** AuthService thêm thông tin username/center dù mỗi request đã tra lại tài khoản trong MySQL.
6. **Cách sửa:** Dùng `sub=user_id`, chỉ giữ role và exp; kiểm tra payload và xác thực lại user bằng ID trong CSDL.
7. **Commit sửa/chạy lại:** [f7969a9](https://github.com/nhan231105/cdtn1-L08/commit/f7969a90aed05fd1ab0132ab4644f521540f73fd); backend suite 57/57 PASS, AuthServiceTest 1/1 PASS.

## Khoảng trống C4

Có ba lỗi thật đã sửa, nhưng **không có lỗi Critical/Major đã được sửa và chứng minh**; C4 chưa đạt. Không nâng mức độ giả tạo. Phép thử Compose ngày 17/11 phát hiện lỗi chưa sửa: `ReportingIntegrationTest` tái chạy 4/6 FAIL (hai assertion số phản hồi, một role-matrix trả 422 thay vì 200, một assertion aggregation); full profile cũng ghi timestamp nhắc lệch 7 giờ. Đây là phát hiện chưa phân loại/chưa có commit sửa nên không được tính là bug đã khắc phục trong log. Cần quay lại chặng sửa code, xác định nguyên nhân múi giờ và reporting, sửa, chạy lại suite và white-box từ đầu trước khi phát hành.
