package com.banking.transactionservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionalResponse;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repositories.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";
    private static final String FRAUD_DETECTED_TOPIC = "fraud.detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /*
     * 
     * SAGA step-1 Initiate Transfer -> Deduct amount from sender in account-service
     * using feign client -> save transaction as processing -> publish event to
     * kafka for fraud check
     * 
     * 
     * 
     * 
     */

    public TransactionalResponse transfer(TransferRequest request)

    {
        log.info("SAGA START -Transfer :{} -> {} amount :{}", request.getSenderAccountNumber(),
                request.getReceiverAccountNumber(), request.getAmount());

        // debit amount from sender
        // get the accountService stuff

        accountServiceClient.deductBalance(request.getSenderAccountNumber(), request.getAmount());
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setDescription(request.getDescription());
        transaction.setRefrenceNumber(UUID.randomUUID().toString());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as processing :{}", savedTransaction.getId());

        /*
         * Publish event to kafka
         */
        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                savedTransaction.getId(),
                savedTransaction.getSenderAccountNumber(),
                savedTransaction.getReceiverAccountNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getDescription());

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, savedTransaction.getId(), event);
        log.info("SAGA step-2 transaction initiated event published. {}", savedTransaction.getId());

        return mapToResponse(savedTransaction);

    }

    // get Transaction

    public TransactionalResponse getTransaction(String transactionId) {
        return mapToResponse(transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found" + transactionId)));
    }

    // get Transaction History

    // get all the transaction from database with the account number (
    // transactionRepository.findBySenderAccountNumberOrderByCreatedAtDes(accountNumber)).stream
    // (process each transaction) .map(this::mapToResponse) (map each transaction to
    // mapToResponse) and after mapping everyhting put it into the list ->
    // controller and then JSON format

    public List<TransactionalResponse> getTransactionHistory(String accountNumber) {
        return transactionRepository.findBySenderAccountNumberOrderByCreatedAtDes(accountNumber).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // verify otp

    // get opt store it into redis and check if the generated opt matched with the
    // redis saved otp

    public TransactionalResponse verifyOTP(String transactionId, String otp) {
        log.info("OTP verification for the transaction :{}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("transaction not found " + transactionId));

        String otpKey = "verification:otp" + transactionId;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            log.warn("OTP expired for the transaction :{}", transactionId);
            compensateTransaction(transaction, "OTP expired - transaction cancelled and amount refunded");
            return mapToResponse(transaction);
        }
        if (!storedOtp.equals(otp)) {
            log.warn("Wrong otp -blocking account and refunding :{}", transactionId);
            redisTemplate.delete(otpKey);
            blockAccountAndCompensate(transaction, "Wrong OTP entered - transaction cancelled "
                    + "account blocked for security " + "Please contact your respective branch");
            return mapToResponse(transaction);
        }
        log.info("OTP verifies -completing transaction :{}", transactionId);
        redisTemplate.delete(otpKey);
        completeTransaction(transaction);
        return mapToResponse(transaction);
    }

    private void compensateTransaction(Transaction transaction, String reason) {
        log.warn("SAGA compensation refunding :{}  amount :{}", transaction.getSenderAccountNumber(),
                transaction.getAmount());

        // sending money back to the send through account setvice

        accountServiceClient.CreditBalance(transaction.getSenderAccountNumber(), transaction.getAmount());
        transaction.setStatus((TransactionStatus.FLAGGED));
        transaction.setFailureReason(reason + "-SAGA compensation executed ,amount refunded at " + LocalDateTime.now());

        transactionRepository.save(transaction);

        // punlish event to kafka - so that notificaiton service will consume it and
        // alert to user

        Map<String, Object> refundEvent = new HashMap<>();
        refundEvent.put("transactioId", transaction.getId());
        refundEvent.put("senderAccountNumber", transaction.getSenderAccountNumber());
        refundEvent.put("amount", transaction.getAmount());
        refundEvent.put("reason", reason);

        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC, transaction.getId(), refundEvent);
        log.info("SAGA compensation complete - {}  refunded to {} - ", transaction.getAmount(),
                transaction.getSenderAccountNumber());

    }

    private void blockAccountAndCompensate(Transaction transaction, String reason) {
        // publish fraud.detected -> Account service will consume this and block the
        // account

    }

    private TransactionalResponse mapToResponse(Transaction transaction) {
        TransactionalResponse response = new TransactionalResponse();
        response.setId(transaction.getId());
        response.setSenderAccountNumber(transaction.getSenderAccountNumber());
        response.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setReferenceNumber(transaction.getRefrenceNumber());
        response.setFailureReason(transaction.getFailureReason());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        return response;

    }

}
