package by.grgu.accountservice.service.impl;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.database.enumm.Role;
import by.grgu.accountservice.database.repository.AccountRepository;
import by.grgu.accountservice.dto.AccDto;
import by.grgu.accountservice.dto.AccountDTO;
import by.grgu.accountservice.service.AccountService;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class AccountServiceImpl implements AccountService, UserDetailsService {

    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate;

    public AccountServiceImpl(AccountRepository accountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> createAccount(AccountRequest request) {
        if (accountExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Account account = mapToAccount(request);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<Account> getAccount(String username) {
        Optional<Account> account = accountRepository.findByUsername(username);
        return account.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<Void> deleteAccount(String username) {
        if (!accountExists(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        accountRepository.deleteByUsername(username);
        return ResponseEntity.noContent().build();
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
        account.setRegistrationDate(LocalDate.now());
        account.setActive(request.isActive());
        account.setRole(mapRole(request.getRole()));
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
                account.isActive(),
                true, // accountNonExpired - не истек ли срок действия учетной записи (если false, то истек)
                true, // credentialsNonExpired - не истекли ли учетные данные (пароль) (если false, то истек)
                true, // accountNonLocked - не заблокирована ли учетная запись  (если false, то заблокирована)
                authorities
        );
    }

    public AccountDTO getAccountData(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + username));

        return new AccountDTO(account.getId(), account.getUsername(), account.getFirstname(), account.getLastname(), account.getEmail());
    }

    public boolean updateAccountFields(Map<String, String> updatedData, String token) {
        System.out.println("🔄 Полученные данные в AccountService: " + updatedData);

        String username = updatedData.get("username");

        System.out.println("🔍 Ищем аккаунт по `username`: " + username);

        Optional<Account> accountOptional = accountRepository.findByUsername(username);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            System.out.println("✅ Найден аккаунт: " + account.getUsername());

            // ✅ Немедленно обновляем `username` в БД, чтобы он был актуальным
            account.setUsername(username);

            account.setFirstname(updatedData.get("firstname"));
            account.setLastname(updatedData.get("lastname"));
            account.setEmail(updatedData.get("email"));

            accountRepository.save(account);
            System.out.println("✅ Аккаунт успешно обновлен!");

            return true;
        } else {
            System.err.println("❌ Ошибка: Аккаунт с username '" + username + "' не найден.");
            return false;
        }
    }


    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token); // ✅ Устанавливаем токен
        return headers;
    }

    public List<AccDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void updateAccountStatus(String username, Map<String, String> status) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + username));

        if (status.containsKey("firstname")) {
            account.setFirstname(status.get("firstname"));
        }
        if (status.containsKey("lastname")) {
            account.setLastname(status.get("lastname"));
        }
        if (status.containsKey("email")) {
            account.setEmail(status.get("email"));
        }
        if (status.containsKey("role")) {
            account.setRole(mapRole(status.get("role")));
        }
        if (status.containsKey("active")) {
            account.setActive(Boolean.parseBoolean(status.get("active")));
        }

        accountRepository.save(account);
    }

    public AccDto getTotalAccountData(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден: " + username));
        return convertToDto(account);
    }

    private AccDto convertToDto(Account account) {
        return new AccDto(
                account.getUsername(),
                account.getBirthDate(),
                account.getFirstname(),
                account.getLastname(),
                account.getEmail(),
                account.getRegistrationDate(),
                account.isActive(),
                account.getRole()
        );
    }
}
