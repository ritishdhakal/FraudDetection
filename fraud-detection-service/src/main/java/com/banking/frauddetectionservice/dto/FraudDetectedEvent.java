package com.banking.frauddetectionservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

// fraud detection service -> kafka
public class FraudDetectedEvent {

    private String transactionId; // this id transaction was fraud
    private BigDecimal amount;
    private String receiverAccountNumber;
    private String senderAccountNumber;
    private BigDecimal riskScore;
    private boolean fraud;

}