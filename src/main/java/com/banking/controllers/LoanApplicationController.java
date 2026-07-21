package com.banking.controllers;

import com.banking.models.dto.request.LoanApplicationRequestDTO;
import com.banking.models.dto.response.LoanApplicationResponseDTO;
import com.banking.models.services.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponseDTO> createLoanApplication(
            @Valid @RequestBody LoanApplicationRequestDTO requestDTO) {
        LoanApplicationResponseDTO responseDTO = loanApplicationService.createLoanApplication(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponseDTO> approveLoanApplication(@PathVariable Long id) {
        LoanApplicationResponseDTO approvedLoan = loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok(approvedLoan);
    }
}
