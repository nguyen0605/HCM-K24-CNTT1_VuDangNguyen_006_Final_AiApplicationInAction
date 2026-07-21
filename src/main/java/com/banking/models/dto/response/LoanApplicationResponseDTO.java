package com.banking.models.dto.response;

import com.banking.models.constant.LoanStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanApplicationResponseDTO {
    private Long id;
    private Double amount;
    private LoanStatus status;
    private Long customerId;
    private String customerName;
}
