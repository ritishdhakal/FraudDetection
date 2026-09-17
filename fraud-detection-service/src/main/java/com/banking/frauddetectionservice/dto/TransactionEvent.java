package com.banking.frauddetectionservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

// things getting from the kafka when transaction.is initiated
public class TransactionEvent {
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private BigDecimal senderBalanceBefore;
    private BigDecimal receiverBalanceBefore;
    private TransactionType type;

}
