package com.banking.exceptions;

public class LoanApprovalException extends RuntimeException {
    public LoanApprovalException(String message) {
        super(message);
    }
}
