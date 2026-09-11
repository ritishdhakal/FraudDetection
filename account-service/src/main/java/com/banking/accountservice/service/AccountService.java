package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private final SecureRandom secureRandom;

    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating an Accoint for {}", request.getEmail());

        // check whether the email exists or not

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Account already exists by email :  " + request.getEmail());
        }

        // otherwise
        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getIntialDeposit());
        account.setAccountNumber(generateAccountNumber());

        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVINGS ? new BigDecimal("100000")
                        : new BigDecimal("500000"));

        Account savedAccount = accountRepository.save(account);
        log.info("Account Created Sucessfully :{}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);

    }
    // create a 12 digit unique code

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);// %formatstart , 0= replace with 0,12 = need 12 digit
            // number and d means decimal

        } while (accountRepository.existsByAccountNumber(accountNumber)); // check if there is user with same account
                                                                          // number if there is no user with the same
                                                                          // account number then generate one otherwiise
                                                                          // no needed
        return accountNumber;
    }
    /*
     * Get Account Start
     */

    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToResponse(account);
    }
    /*
     * Get Account end
     */

    /*
     * Get Credit balance Start
     */

    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting {} to account {]", amount, accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        log.info("Balance credited. New Balance :{}", account.getBalance());
        accountRepository.save(account);

        log.info("Balance updated and new balance is  {}", account.getBalance());
    }

    /*
     * Get Creadit balance END
     */

    /*
     * GEt balance start
     */

    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getBalance();
    }

    /*
     * GEt balance end
     */

    /*
     * During the transaction if the fraud is detected then we need to block the
     * account
     * 
     */
    public void blockAccount(String accountNumber) {
        log.info("Blocling Account :{}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);

        log.info("Account blocked :{} ", accountNumber);
    }

    /*
     * 
     * Deduct balance
     * this is called by transaction service
     * 
     * 
     */
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting the balance  {} from the account  {}", amount, accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active" + accountNumber);

        }
        // check whether there issufficient amount or not

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for account " + accountNumber);

        }
        account.setBalance(account.getBalance().subtract(amount));
        log.info("Balance updated and new balance is  :{}", account.getBalance());

    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setAccountType(account.getAccountType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());

        return response;

    }
}
