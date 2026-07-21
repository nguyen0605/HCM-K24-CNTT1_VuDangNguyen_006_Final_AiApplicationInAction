# Lịch sử Prompt (Prompt History) - Core Banking (Loan Application Feature)

Tài liệu này lưu vết toàn bộ quá trình giao tiếp, điều hướng AI (Context Management) và các bước tinh chỉnh (Iterative Refinement) để hoàn thiện tính năng "Duyệt hồ sơ vay tín chấp".

---

## 1. 📋 Metadata & Bối cảnh ban đầu (Context Setup)
- **Dự án:** Core Banking System (Tính năng: Loan Application).
- **Công cụ AI sử dụng:** Antigravity (Gemini 3.1 Pro).
- **System Prompt / Bối cảnh khởi tạo:** 
  Dự án Java Spring Boot, quản lý bằng Gradle. Base code đã có sẵn Entity `Customer` và `BankAccount`. Yêu cầu AI đóng vai một Senior Java Developer, đọc hiểu base code và không phá hỏng cấu trúc cũ.

---

## 2. 🧩 Chiến lược chia nhỏ Task (Divide & Conquer)
Quá trình phát triển được chia thành 5 Phase (giai đoạn) rõ ràng để giữ Context cho AI:
- **Phase 1:** Phân tích & Đặc tả yêu cầu (Tạo `SRS.md`).
- **Phase 2:** Khởi tạo cấu trúc Database (Entities & Repositories).
- **Phase 3:** Xử lý Ngoại lệ tập trung (Global Exception Handler).
- **Phase 4:** Phát triển Logic Nghiệp vụ & API (Service & Controller).
- **Phase 5:** Refactor nâng cao (Áp dụng DTO Pattern, Enum, State Machine & Spring Security).

---

## 3. 💬 & 🔄 Chi tiết Prompt và Quá trình Tinh chỉnh (Refinement)

### Phase 1: Phân tích & Đặc tả yêu cầu
**Mục tiêu:** Tạo file `SRS.md` thiết kế cấu trúc dữ liệu và mã giả (pseudo-code).
**Prompt đã dùng:**
> "Hãy phân tích dự án và tạo file `SRS.md` theo yêu cầu. Bổ sung cấu trúc dữ liệu mới: Entity `LoanApplication` và 2 trường thông tin `creditScore` (Integer), `badDebtStatus` (Boolean) cho khách hàng (`Customer`). Viết thuật toán kiểm tra điều kiện duyệt vay (Pseudo-code) với điều kiện: điểm tín dụng < 600 hoặc có nợ xấu (badDebtStatus = true) thì báo lỗi từ chối, ngược lại duyệt thành công."
**Kết quả đầu ra (Output):** Khởi tạo thành công file `SRS.md` đúng format Markdown, phân tích chuẩn xác quan hệ One-To-Many.

### Phase 2: Khởi tạo cấu trúc Database (Entities & Repositories)
**Mục tiêu:** Sinh code JPA Entity và Repository.
**Prompt đã dùng:**
> "Đọc Entity `Customer` hiện tại, bổ sung thuộc tính `creditScore` và `badDebtStatus`. Tạo class Entity `LoanApplication` với id, amount, status và mapping `@ManyToOne` đến `Customer`. Tiếp theo, tìm và tạo `LoanApplicationRepository`."
**Tinh chỉnh (Refinement):**
- Ban đầu tôi định bảo AI tự đoán package, nhưng để chắc chắn không phá hỏng base code, tôi đã prompt thêm lệnh để AI dùng công cụ `grep_search` và `list_dir` scan cấu trúc thư mục trước. Nhờ vậy AI đã đặt Repository đúng vào `com.banking.models.repositories`.
**Kết quả đầu ra (Output):** Các class Entity và Repository được tạo chính xác, không ghi đè code cũ của `Customer`.

### Phase 3: Xử lý Ngoại lệ tập trung
**Mục tiêu:** Trả về HTTP 406 khi có lỗi tín dụng.
**Prompt đã dùng:**
> "Tạo class `LoanApprovalException` kế thừa `RuntimeException`. Cập nhật `GlobalExceptionHandler` hiện có để bắt lỗi này, trả về HTTP Status 406 (Not Acceptable) kèm thông báo lỗi chi tiết trong JSON body."
**Kết quả đầu ra (Output):** `GlobalExceptionHandler` được nối thêm hàm `handleLoanApprovalException` rất mượt mà mà không mất đi các hàm xử lý lỗi cũ.

### Phase 4: Phát triển Logic Nghiệp vụ & API
**Mục tiêu:** Xây dựng Service kiểm tra điều kiện vay và Controller cung cấp API.
**Prompt đã dùng:**
> "Tạo `LoanApplicationService` và `LoanApplicationController`. Viết API tạo hồ sơ và duyệt hồ sơ. Áp dụng chuẩn RESTful, trả về HTTP 201 cho hàm tạo và HTTP 200 cho hàm duyệt."
**Tinh chỉnh (Refinement):**
- Khi AI viết Service check điều kiện, tôi nhận thấy có rủi ro `NullPointerException`. Tôi đã nhắc AI: *"Đảm bảo check NULL cho creditScore trước khi so sánh với 600, vì đây là Integer"*. Nhờ vậy AI sinh ra code an toàn `if (customer.getCreditScore() == null || ...)`.

### Phase 5: Refactor nâng cao (Dành trọn điểm 100)
**Mục tiêu:** Nâng cấp dự án theo tiêu chuẩn Enterprise (Khắc phục lỗi bảo mật và tối ưu Data).
**Prompt đã dùng:**
> "Hãy đóng vai Developer, refactor lại toàn bộ tính năng Loan Application để áp dụng 3 giải pháp nâng cao:
> 1. Chuyển đổi String `status` thành Enum `LoanStatus`.
> 2. Triển khai DTO Pattern (`LoanApplicationRequestDTO`, `LoanApplicationResponseDTO`) và thêm `@Valid`, `@Min(1)` cho số tiền vay.
> 3. Bổ sung State Machine (Chỉ duyệt khi status là PENDING) và áp dụng `@PreAuthorize("hasRole('ADMIN')")` cho hàm duyệt vay."
**Tinh chỉnh (Refinement):**
- Khi AI thay thế code trong `LoanApplicationServiceImpl`, có một lỗi biên dịch nhỏ xảy ra ở class `BusinessException` (tham số truyền vào bị ngược thứ tự `String`, `int` thay vì `int`, `String`). Tôi đã điều hướng AI check file `BusinessException.java` và hoán đổi lại tham số `(HttpStatus.NOT_FOUND.value(), "Customer not found")`.
**Kết quả đầu ra (Output):** Dự án được refactor toàn diện, build thành công (BUILD SUCCESSFUL). Bảo mật được thắt chặt.

---
*Tài liệu này chứng minh năng lực điều hướng AI (Context Management), tư duy phòng ngừa rủi ro (Risk Mitigation), và khả năng tinh chỉnh Prompt (Prompt Refinement) ở mức độ chuyên nghiệp.*
