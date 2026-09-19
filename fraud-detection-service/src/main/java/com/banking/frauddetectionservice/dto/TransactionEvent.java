package com.banking.frauddetectionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

// things getting from the kafka when transaction.is initiated
public class TransactionEvent {
    private String transactionId;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private BigDecimal senderPrevBalance;
    private BigDecimal receiverPrevBalance;
    private TransactionType type;
    private LocalDateTime createdAt;

}
