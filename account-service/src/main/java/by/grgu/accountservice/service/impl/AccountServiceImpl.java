package by.grgu.accountservice.service.impl;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.database.enumm.Role;
import by.grgu.accountservice.database.repository.AccountRepository;
import by.grgu.accountservice.dto.AccountDTO;
import by.grgu.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService, UserDetailsService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ResponseEntity<Void> createAccount(AccountRequest request) {
        System.out.println("Аккаунт реквест: " + request);
        if (accountExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        Account account = mapToAccount(request);
        accountRepository.save(account);
        System.out.println("Аккаунт готов: " + account);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(account.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                account.getUsername(),
                account.getPassword(),
                account.isActive(), // Активен ли аккаунт
                true, // accountNonExpired - не истек ли срок действия учетной записи (если false, то истек)
                true, // credentialsNonExpired - не истекли ли учетные данные (пароль) (если false, то истек)
                true, // accountNonLocked - не заблокирована ли учетная запись  (если false, то заблокирована)
                authorities
        );
    }

    public AccountDTO getAccountData(String username) {
        // ✅ Проверяем, есть ли аккаунт в БД
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("❌ Аккаунт не найден: " + username));

        return new AccountDTO(account.getUsername(), account.getFirstname(), account.getLastname(), account.getEmail());
    }

    public void updateAccountFields(String username, Map<String, String> updatedData) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("❌ Аккаунт не найден: " + username));

        updatedData.forEach((field, value) -> {
            switch (field) {
                case "firstname":
                    account.setFirstname(value);
                    break;
                case "lastname":
                    account.setLastname(value);
                    break;
                case "email":
                    account.setEmail(value);
                    break;
                case "username":
                    account.setUsername(value);
                    break;
                default:
                    throw new IllegalArgumentException("❌ Недопустимое поле: " + field);
            }
        });

        accountRepository.save(account); // ✅ Сохраняем все изменения
    }

}
