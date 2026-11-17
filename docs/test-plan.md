# Test Plan — Smart CRM L8

**[GIẢ ĐỊNH – cần đối chiếu đề BT3/buổi 13]:** Chưa có đề BT3 chính thức và tài liệu buổi 13 trong workspace. Bản này theo cấu trúc bốn mục và năm loại test nêu trong prompt.

## 1. Mục tiêu và phạm vi

Kiểm tra luồng CSAT sau bảo hành: mẫu khảo sát, lời mời, phản hồi bất biến, đăng nhập/phân quyền, báo cáo, export, khởi động lặp và giao diện React. MySQL là hệ quản trị thật trong test; gateway SMS/Zalo được mô phỏng.

## 2. Cách thiết kế test

Phân nhóm thành năm loại: luồng đúng, biên, dữ liệu sai, bảo mật/chất lượng dữ liệu, và lặp lại/idempotency. Mỗi test API dùng fixture tổng hợp; case trình duyệt tạo ticket riêng trong database test. Kiểm tra chuỗi SQL injection qua tham số kiểu số phải trả 400, không thực thi SQL. Bộ system case được ghi tại `frontend/e2e/system-qa.spec.js`; test claim JWT tại `AuthServiceTest`.

## 3. Môi trường và dữ liệu

Backend Spring Boot/Java 21, MySQL 8.0, React/Vite/Node 24, Playwright Chromium. `scripts/test.ps1` khởi tạo MySQL test mới trên loopback cổng 3307 và giữ dữ liệu test trong thư mục tạm; không dùng schema đang làm việc. Dữ liệu V2 là giả lập, không gọi nhà cung cấp SMS/Zalo thật.

## 4. Tiêu chí hoàn tất và giới hạn

Mục tiêu là 10–12 test case, đủ tám cột trong Test Report, có đủ năm loại test; backend không có failure/error/skip; frontend unit test và production build thành công; lần chạy Flyway thứ hai không đổi số dòng seed. Bản chạy container sạch chỉ được đánh dấu đạt sau khi Docker Compose thực sự chạy. Penetration test, kiểm thử nhiều instance, SMS/Zalo thật và kiểm thử người dùng không thuộc phạm vi BT3 này.
