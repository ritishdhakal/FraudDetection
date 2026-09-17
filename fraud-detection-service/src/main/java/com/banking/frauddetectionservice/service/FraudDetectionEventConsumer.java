package com.banking.frauddetectionservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.dto.TransactionEvent;

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
public class FraudDetectionEventConsumer {

    @KafkaListener(topics = "transaction.initiated", groupId = "fraud-detection-group")

    // function that receives the data from kafka and basically using
    // dto/transactionevent file
    public void counsumeTransactionInitiated(TransactionEvent event) {
        log.info("Received transaction.inititated event");
        log.info("Transaction ID :{}", event.getTransactionId());
        log.info("Sender AccountNumber :{}", event.getSenderAccountNumber());
        log.info("Receiver AccountNumber :{}", event.getReceiverAccountNumber());
        log.info("Amount: {}", event.getAmount());
        log.info("Sender previous balance: {}",
                event.getSenderBalanceBefore());

        log.info("Receiver previous balance: {}",
                event.getReceiverBalanceBefore());
        log.info("Type: {}", event.getType());

    }

}
