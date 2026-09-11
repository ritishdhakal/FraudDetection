package com.banking.accountservice.dto;

import java.math.BigDecimal;

import com.banking.accountservice.entity.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Name is required")
    private String accountHolderName;

    @NotBlank(message = "Email is required")
    @Email(message = "Format not matched")
    private String email;

    @NotBlank(message = "Phonen number is required")
    private String phone;

    @NotNull(message = "Account type is required")

    private AccountType accountType;

    @NotNull(message = "Initial deposit is required")
    @Positive(message = "Amount must be in positive")

    private BigDecimal intialDeposit;
}
