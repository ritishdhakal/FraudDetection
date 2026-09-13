package com.banking.transactionservice.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.repositories.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {

    // verification.required from fraud-detection-service
    // generate otp

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TRANSACTION_OTP_GENERATED_TOPIC = "transaction.otp.generated";
    private static final long OTP_EXPIRY_MINUTES = 5;

    @KafkaListener(topics = "verification.required")

    public void consumeVerificationRequired(
            @Payload Map<String, Object> payload) {
        try {
            String transactionId = (String) payload.get("transactionId");
            String accountNumber = (String) payload.get("accountNumber");
            String reason = (String) payload.get("reason");

            log.info("Verificatoin required - transaction :{} reason :{}", transactionId, reason);

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found" + transactionId));

            // only generate otp if transaction id is processing

            if (transaction.getStatus() != TransactionStatus.PROCESSING) {
                log.warn("Transaction:{} not processing ", transactionId);
                return;

            }

            // generate an otp and save it to the redis

            String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);

            // store the otp into redis

            String otpKey = "verification:otp" + transactionId;
            redisTemplate.opsForValue().set(otpKey,
                    otp,
                    Duration.ofMinutes(OTP_EXPIRY_MINUTES));
            // update the status

            transaction.setStatus(TransactionStatus.PENDING_VERIFICATION);
            transactionRepository.save(transaction);

            log.info("OTP generated for the transaction :{} expires in {} min", transactionId, OTP_EXPIRY_MINUTES);

            // notify user
            Map<String, Object> otpEvent = new HashMap<>();
            otpEvent.put("transactionId", transactionId);
            otpEvent.put("accountNumber", accountNumber);
            otpEvent.put("reason", reason);
            otpEvent.put("opt", otp);

            otpEvent.put("amount", payload.get("amount"));

            kafkaTemplate.send(TRANSACTION_OTP_GENERATED_TOPIC, transactionId, otpEvent);

        } catch (Exception e) {
            log.error("Error :{}", e.getMessage());
        }
    }

    @KafkaListener(topics = "fraud.check.clean")

    public void consumeFraudCheckResult(
            @Payload Map<String, Object> payload) {
        try {
            String transactioId = (String) payload.get("transactionId");
            transactionService.processCleanResult(transactioId);
        } catch (Exception e) {
            log.error("Error processing fraud check :{}", e.getMessage());
        }
    }
}
