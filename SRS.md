# Software Requirements Specification (SRS) - Core Banking (Loan Application Feature)

*Phiên bản nâng cao (Enterprise Standard) - Đảm bảo tính Toàn vẹn, Bảo mật và Khả năng Truy vết.*

## 1. Thiết kế cấu trúc dữ liệu mới

### Bổ sung thông tin cho Entity `Customer`
Khách hàng sẽ được lưu trữ thêm 2 thuộc tính (giữ nguyên các trường hiện có):
- `creditScore` (Integer): Điểm tín dụng của khách hàng. Có thể Null nếu khách hàng chưa từng giao dịch.
- `badDebtStatus` (Boolean): Trạng thái nợ xấu (true nếu có nợ xấu, false/null nếu không có).

### Tạo mới Entity `LoanApplication` (Hồ sơ vay)
Nhằm đáp ứng tiêu chuẩn kiểm toán ngân hàng (Auditability) và độ chính xác tài chính, cấu trúc bảng được thiết kế như sau:

**Các trường Nghiệp vụ (Business Fields):**
- `id` (Long, Primary Key): Mã hồ sơ vay.
- `amount` (BigDecimal): Số tiền vay. *(Ghi chú SA: Tuyệt đối dùng BigDecimal thay vì Double để tránh sai số dấu phẩy động trong giao dịch tài chính).*
- `status` (Enum: LoanStatus): Trạng thái hồ sơ vay, gồm `PENDING` (Chờ duyệt), `APPROVED` (Đã duyệt), `REJECTED` (Từ chối).
- `customer_id` (Long): Khóa ngoại liên kết tới Customer.

**Các trường Truy vết (Audit Fields):**
- `createdAt` (LocalDateTime): Thời gian tạo hồ sơ.
- `updatedAt` (LocalDateTime): Thời gian cập nhật trạng thái cuối cùng.
- `approvedBy` (String/Long): ID hoặc Username của Admin đã thực hiện lệnh duyệt/từ chối.

### Mối quan hệ (Relationship)
- Một `Customer` có thể tạo nhiều `LoanApplication`.
- Chiều ánh xạ: **Many-To-One** từ phía `LoanApplication` tham chiếu tới `Customer` (Sử dụng `FetchType.LAZY` để tối ưu hiệu năng truy vấn).

---

## 2. Thuật toán kiểm tra điều kiện duyệt vay (Pseudo-code)

Thuật toán đã được tích hợp **State Machine (Máy trạng thái)** để ngăn chặn rủi ro xử lý trùng lặp và **Concurrency (Tương tranh)**.

```pseudo
FUNCTION approveLoanApplication(loanId, adminId)
    // Bước 1: Tìm hồ sơ vay (Có thể áp dụng Optimistic Locking @Version để tránh Race Condition)
    loanApplication = findLoanById(loanId)
    IF loanApplication is null THEN
        THROW Error(404, "Không tìm thấy hồ sơ vay")
    END IF

    // Bước 2: State Machine Validation (Chặn duyệt 2 lần)
    IF loanApplication.status != "PENDING" THEN
        THROW Error(406, "Hồ sơ đã được xử lý (APPROVED/REJECTED) trước đó!")
    END IF

    // Bước 3: Lấy thông tin khách hàng từ hồ sơ vay
    customer = loanApplication.getCustomer()

    // Bước 4: Kiểm tra điều kiện điểm tín dụng (Check Null safety)
    IF customer.creditScore is null OR customer.creditScore < 600 THEN
        THROW Error(406, "Từ chối: Điểm tín dụng không đủ (dưới 600)")
    END IF

    // Bước 5: Kiểm tra điều kiện nợ xấu
    IF customer.badDebtStatus == true THEN
        THROW Error(406, "Từ chối: Khách hàng đang có nợ xấu")
    END IF

    // Bước 6: Nếu thỏa mãn tất cả, tiến hành duyệt và ghi nhận Audit
    loanApplication.status = "APPROVED"
    loanApplication.approvedBy = adminId
    loanApplication.updatedAt = CURRENT_TIMESTAMP
    
    save(loanApplication)
    
    RETURN loanApplication
END FUNCTION
```

---

## 3. Đặc tả Bề mặt Giao tiếp (API Specifications)

Hệ thống cung cấp 2 endpoints RESTful cho Khách hàng và Admin, tuân thủ chặt chẽ việc ẩn giấu dữ liệu nhạy cảm qua DTO Pattern.

### 3.1. API Tạo Hồ sơ Vay (Dành cho Khách hàng)
- **Endpoint:** `POST /api/loans`
- **Quyền hạn:** User/Customer (Hoặc Public tùy business).
- **Request Body (LoanApplicationRequestDTO):**
  - `customerId` (Long, Not Null)
  - `amount` (BigDecimal, Min: 1 - Chặn nhập số âm)
- **Response (201 Created):** `LoanApplicationResponseDTO` (Chứa `id`, `amount`, `status="PENDING"`, `customerId`).

### 3.2. API Duyệt Hồ sơ Vay (Dành cho Admin)
- **Endpoint:** `POST /api/loans/{id}/approve`
- **Quyền hạn:** Yêu cầu xác thực `@PreAuthorize("hasRole('ADMIN')")`.
- **Request Variable:** `id` (Mã hồ sơ vay truyền qua URL).
- **Response (200 OK):** Trả về `LoanApplicationResponseDTO` với trạng thái `APPROVED`.
- **Exception (406 Not Acceptable):** 
  Trường hợp điểm tín dụng < 600 hoặc nợ xấu, hệ thống sẽ trigger `LoanApprovalException`. Cấu trúc `GlobalExceptionHandler` sẽ can thiệp và trả về:
  ```json
  {
      "status": 406,
      "message": "Từ chối: Khách hàng đang có nợ xấu"
  }
  ```
