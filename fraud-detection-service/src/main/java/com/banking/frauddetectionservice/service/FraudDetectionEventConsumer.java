package com.banking.frauddetectionservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.dto.FraudPredictionRequest;
import com.banking.frauddetectionservice.dto.FraudPredictionResponse;
import com.banking.frauddetectionservice.dto.TransactionEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

/*
 * 
 * listen to transaction.initiated event from transaction service and every
 * transction goes through fraud check before completign
 * 
 * 
 */
@RequiredArgsConstructor
public class FraudDetectionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    // from the transaction service

    @KafkaListener(topics = "transaction.initiated", groupId = "fraud-detection-service-group")

    // function that receives the data from kafka and basically using
    // dto/transactionevent file
    public void counsumeTransactionInitiated(TransactionEvent event) {
        log.info("Received transaction.inititated event");
        log.info("Transaction ID :{}", event.getTransactionId());
        log.info("Sender AccountNumber :{}", event.getSenderAccountNumber());
        log.info("Receiver AccountNumber :{}", event.getReceiverAccountNumber());
        log.info("Amount: {}", event.getAmount());
        log.info("Sender previous balance: {}",
                event.getSenderPrevBalance());

        log.info("Receiver previous balance: {}",
                event.getReceiverPrevBalance());
        log.info("Type: {}", event.getType());

        FraudPredictionRequest request = fraudDetectionService.builPredictionRequest(event);
        fraudDetectionService.sendForFraudCheck(request);
        log.info("Sent transaction to ML model");
    }

    // consume again from the pyhton after result
    @KafkaListener(topics = "fraud.detection.result", groupId = "fraud-detection-service-group")
    public void consumePredictionResult(FraudPredictionResponse response) {
        log.info("Received fraud prediction result");

        log.info("TransactionId :{}", response.getTransactionId());
        log.info("Is fraud :{} ", response.isFraud());
        log.info("probability :{}", response.getProbability());

        // check if it is fraud or not
        if (response.isFraud()) {
            log.info("Fraud detected for the transaciton : ", response.getTransactionId());

            // sebd this event to the service

            fraudDetectionService.sendFraudDetectedEvent(response);
        }

        else {
            log.info("Clean transaciton");
            fraudDetectionService.sendFraudApprovedEvent(response);

        }
    }

}
