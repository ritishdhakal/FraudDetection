package com.banking.frauddetectionservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.dto.FraudPredictionRequest;
import com.banking.frauddetectionservice.dto.TransactionEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {
    /// function for feature engineering because
    /// we have senderPrevBalanve, reveiver prev balance stuff in java side where in
    /// ml side we have oldBalanceOrg, oldBalanceDest

    private static final String FRAUD_CHECK_REQUEST_TOPIC = "fraud.prediction.request";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudPredictionRequest builPredictionRequest(
            TransactionEvent event) {
        FraudPredictionRequest request = new FraudPredictionRequest();

        // since steps is the Step denotes a portion of the time period

        // so I am planning to do like if it's let's say 2 -2:59 then stepis 2 14-14:59
        // step = 14 so we getting created at and converting that ot hours

        request.setStep(event.getCreatedAt().getHour()); // temporary
        request.setAmount(event.getAmount());

        // sender prev balance

        request.setSenderPrevBalance(event.getSenderPrevBalance());
        request.setReceiverPrevBalance(event.getReceiverPrevBalance());

        // one hot encoding for transaction type

        request.setType_CASH_OUT(0);
        request.setType_DEBIT(0);
        request.setType_PAYMENT(0);
        request.setType_TRANSFER(0);

        switch (event.getType()) {
            case WITHDRAWAL:
                request.setType_CASH_OUT(1);
                break;
            case PAYMENT:
                request.setType_PAYMENT(1);
                break;

            case TRANSFER:
                request.setType_TRANSFER(1);
                break;
            default:
                break;
        }
        log.info("ML prediciton features :{}", request);
        return request;

    }

    public void sendForFraudCheck(FraudPredictionRequest request) {
        kafkaTemplate.send(FRAUD_CHECK_REQUEST_TOPIC, request);
    }

}
