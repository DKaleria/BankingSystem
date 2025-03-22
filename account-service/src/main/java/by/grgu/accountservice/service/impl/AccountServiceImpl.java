package by.grgu.accountservice.service.impl;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.database.enumm.Role;
import by.grgu.accountservice.database.repository.AccountRepository;
import by.grgu.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ResponseEntity<Void> createAccount(AccountRequest request) {
        if (accountExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        Account account = mapToAccount(request);
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

    private Account mapToAccount(AccountRequest request) {
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setBirthDate(request.getBirthDate());
        account.setFirstname(request.getFirstname());
        account.setLastname(request.getLastname());
        account.setPassword(request.getPassword());
        account.setEmail(request.getEmail());
        account.setRegistrationDate(LocalDate.now()); // Установите текущую дату
        account.setActive(request.isActive());
        account.setRole(mapRole(request.getRole())); // Логика маппинга роли
        return account;
    }

    private Role mapRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}