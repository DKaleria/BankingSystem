package by.grgu.accountservice.service.impl;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.database.repository.AccountRepository;
import by.grgu.accountservice.service.AccountService;
import by.grgu.accountservice.usecasses.mapper.AccountMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public ResponseEntity<Void> createAccount(AccountRequest request) {
        if (accountExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        Account account = accountMapper.toAccount(request);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    public ResponseEntity<Account> getAccount(String username) {
        Optional<Account> account = accountRepository.findByUsername(username);
        return account.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<Void> deleteAccount(String username) {
        if (!accountExists(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }

        accountRepository.deleteByUsername(username);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    private boolean accountExists(String username) {
        return accountRepository.findByUsername(username).isPresent();
    }
}