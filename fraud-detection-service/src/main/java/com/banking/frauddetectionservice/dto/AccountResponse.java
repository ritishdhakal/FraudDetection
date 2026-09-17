package com.banking.frauddetectionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AccountResponse {
    private String accountNumber;
    private String balance;
}
