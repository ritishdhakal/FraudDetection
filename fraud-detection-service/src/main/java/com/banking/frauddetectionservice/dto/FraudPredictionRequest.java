package com.banking.frauddetectionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

// sending this to the ML model or ML is requesting this
public class FraudPredictionRequest {

        private Integer steps;
        private String amount;
        private String oldbalanceOrg;
        private String oldbalanceDest;
        private int type_CASH_OUT;

        private int type_DEBIT;

        private int type_PAYMENT;

        private int type_TRANSFER;

}
