package com.banking.frauddetectionservice.service;

import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.dto.AccountResponse;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor

public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;

}
