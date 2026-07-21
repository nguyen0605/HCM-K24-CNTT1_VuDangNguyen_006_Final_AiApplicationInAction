package com.banking.models.services.impl;

import com.banking.exceptions.BusinessException;
import com.banking.exceptions.LoanApprovalException;
import com.banking.models.constant.LoanStatus;
import com.banking.models.dto.request.LoanApplicationRequestDTO;
import com.banking.models.dto.response.LoanApplicationResponseDTO;
import com.banking.models.entities.Customer;
import com.banking.models.entities.LoanApplication;
import com.banking.models.repositories.CustomerRepository;
import com.banking.models.repositories.LoanApplicationRepository;
import com.banking.models.services.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;

    @Override
    public LoanApplicationResponseDTO createLoanApplication(LoanApplicationRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Customer not found"));

        LoanApplication loanApplication = LoanApplication.builder()
                .amount(requestDTO.getAmount())
                .status(LoanStatus.PENDING)
                .customer(customer)
                .build();

        LoanApplication saved = loanApplicationRepository.save(loanApplication);
        return mapToResponseDTO(saved);
    }

    @Override
    public LoanApplicationResponseDTO approveLoanApplication(Long loanId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Loan application not found"));

        if (loanApplication.getStatus() != LoanStatus.PENDING) {
            throw new LoanApprovalException("Hồ sơ đã được xử lý!");
        }

        Customer customer = loanApplication.getCustomer();

        if (customer.getCreditScore() == null || customer.getCreditScore() < 600) {
            throw new LoanApprovalException("Từ chối: Điểm tín dụng không đủ (dưới 600)");
        }

        if (Boolean.TRUE.equals(customer.getBadDebtStatus())) {
            throw new LoanApprovalException("Từ chối: Khách hàng đang có nợ xấu");
        }

        loanApplication.setStatus(LoanStatus.APPROVED);
        LoanApplication saved = loanApplicationRepository.save(loanApplication);
        return mapToResponseDTO(saved);
    }

    private LoanApplicationResponseDTO mapToResponseDTO(LoanApplication loanApplication) {
        return LoanApplicationResponseDTO.builder()
                .id(loanApplication.getId())
                .amount(loanApplication.getAmount())
                .status(loanApplication.getStatus())
                .customerId(loanApplication.getCustomer().getId())
                .customerName(loanApplication.getCustomer().getFullName())
                .build();
    }
}
