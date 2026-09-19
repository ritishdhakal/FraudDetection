package com.banking.frauddetectionservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// this is the thing i am getting from the ML model
public class FraudPredictionResponse {
    private String transactionId;
    private boolean isFraud;
    private BigDecimal probability;

}
