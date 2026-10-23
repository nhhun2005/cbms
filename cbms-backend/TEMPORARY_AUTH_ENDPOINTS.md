# Danh Sách Endpoint Tạm Thời Truyền User ID Qua Request Body

Tài liệu này ghi nhận các API endpoint hiện đang tạm thời nhận ID của người thực thi (`userId` / `creatorUserId`) trực tiếp từ Request Body để phục vụ kiểm thử và phát triển trong giai đoạn `SecurityConfig` đang cấu hình `permitAll()`.

Khi phân hệ Xác thực & Phân quyền (Spring Security + JWT / Session) được hoàn thiện và kích hoạt, các endpoint này cần được refactor để tự động trích xuất thông tin người dùng từ `SecurityContextHolder` / `Authentication` token thay vì nhận từ client.

---

| STT | Endpoint | Method | DTO Request | Thuộc tính tạm thời | Mô tả nghiệp vụ | Hướng refactor khi có Spring Security |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- |
| 1 | `/api/v1/orders` | `POST` | `CreateOrderRequest` | `creatorUserId` (UUID) | Người tạo hóa đơn / đơn hàng (tự động gán `OrderAssignment` với role `CREATOR`). | Xóa trường `creatorUserId` khỏi DTO; lấy trực tiếp User từ `@AuthenticationPrincipal` hoặc `SecurityContext`. |

---
*Ghi chú: Cập nhật file này mỗi khi có thêm endpoint mới cần thông tin người thực thi mà phải truyền qua Request Body trong giai đoạn dev.*
