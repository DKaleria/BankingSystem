package by.grgu.accountservice.service;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.dto.AccountDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AccountService {
    ResponseEntity<Void> createAccount(AccountRequest request);

    ResponseEntity<Account> getAccount(String username);

    ResponseEntity<Void> deleteAccount(String username);

    AccountDTO getAccountData(String username);

    void updateAccountFields(String username, Map<String, String> updatedData);
}
