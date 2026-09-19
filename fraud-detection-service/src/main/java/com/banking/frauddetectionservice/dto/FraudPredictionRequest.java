package com.banking.frauddetectionservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

// sending this to the ML model or ML is requesting this
public class FraudPredictionRequest {

        private String transacitonId;
        private Integer step;
        private BigDecimal amount;
        private BigDecimal senderPrevBalance;
        private BigDecimal receiverPrevBalance;
        private int type_CASH_OUT;

        private int type_DEBIT;

        private int type_PAYMENT;

        private int type_TRANSFER;

}
