package com.banking.transactionservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Sender acount is required")

    private String senderAccountNumber;

    @NotBlank(message = "Receiver Account is required")
    private String receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount cannot be in negative")
    private BigDecimal amount;
    private String description;

}
