package com.banking.transactionservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.transactionservice.dto.TransactionalResponse;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Slf4j
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor

public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionalResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionalResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));

    }

    @GetMapping("/account/{accountNumber}")

    public ResponseEntity<List<TransactionalResponse>> getTransactionHistory(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
    }

    // verify otp

    @PostMapping("/{transactionId}/verify")
    public ResponseEntity<TransactionalResponse> verifyOTP(
            @PathVariable String transactionId,
            @RequestParam String otp) {
        log.info("Verifying :{}  otp :{}", transactionId, otp);
        return ResponseEntity.ok(transactionService.verifyOTP(transactionId, otp));
    }

}
