package com.banking.frauddetectionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.banking.frauddetectionservice.dto.AccountResponse;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/{accountNumber}")
    AccountResponse getAccount(
            @PathVariable("accountNumber") String accountNumber);

}