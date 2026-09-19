package com.banking.transactionservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.transactionservice.entity.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInitiatedEvent {

    private String transactionId;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
    private BigDecimal senderPrevBalance;
    private BigDecimal receiverPrevBalance;
}