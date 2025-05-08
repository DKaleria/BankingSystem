package by.grgu.accountservice.service;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import org.springframework.http.ResponseEntity;
public interface AccountService {
    ResponseEntity<Void> createAccount(AccountRequest request);

    ResponseEntity<Account> getAccount(String username);

    ResponseEntity<Void> deleteAccount(String username);
}
