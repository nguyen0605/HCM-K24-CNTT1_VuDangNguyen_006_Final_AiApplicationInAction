package com.banking.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanApplicationRequestDTO {

    @NotNull(message = "Customer ID không được để trống")
    private Long customerId;

    @NotNull(message = "Số tiền vay không được để trống")
    @Min(value = 1, message = "Số tiền vay phải lớn hơn 0")
    private Double amount;
}
