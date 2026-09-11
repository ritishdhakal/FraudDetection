package com.banking.accountservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.accountservice.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

    boolean xistsByEmail(String email); // user with one email - one account

    boolean existsByAccountNumber(String accoutNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

}
