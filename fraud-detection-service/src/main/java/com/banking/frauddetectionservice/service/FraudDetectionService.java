package com.banking.frauddetectionservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.dto.FraudDetectedEvent;
import com.banking.frauddetectionservice.dto.FraudPredictionRequest;
import com.banking.frauddetectionservice.dto.FraudPredictionResponse;
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

    /*
     * If it is fraud or clean like any transactions then we need to send the kafka
     * event to transactionservice that is going to consme it
     */

    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_RESULT_TOPIC = "fraud.check.clean";

    public void sendFraudDetectedEvent(FraudPredictionResponse response) {

        log.info("transaction:{} fraud checked is completed :{}", response.getTransactionId());
        FraudDetectedEvent event = new FraudDetectedEvent(
                response.getTransactionId(),
                response.isFraud(),
                response.getProbability());
        kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, response.getTransactionId(), event);

    }

    // if the result is clean

    public void sendFraudApprovedEvent(FraudPredictionResponse response) {

        log.info("transaction:{} fraud checked is completed :{}", response.getTransactionId());
        FraudDetectedEvent event = new FraudDetectedEvent(
                response.getTransactionId(),
                response.isFraud(),
                response.getProbability());
        kafkaTemplate.send(FRAUD_CHECK_CLEAN_RESULT_TOPIC, response.getTransactionId(), event);

    }

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudPredictionRequest builPredictionRequest(
            TransactionEvent event) {
        FraudPredictionRequest request = new FraudPredictionRequest();

        request.setTransacitonId(event.getTransactionId());
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
