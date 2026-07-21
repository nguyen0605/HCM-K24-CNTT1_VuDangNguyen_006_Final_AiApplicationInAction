package com.banking.models.services;

import com.banking.models.entities.LoanApplication;

import com.banking.models.dto.request.LoanApplicationRequestDTO;
import com.banking.models.dto.response.LoanApplicationResponseDTO;

public interface LoanApplicationService {
    LoanApplicationResponseDTO createLoanApplication(LoanApplicationRequestDTO requestDTO);
    LoanApplicationResponseDTO approveLoanApplication(Long loanId);
}
