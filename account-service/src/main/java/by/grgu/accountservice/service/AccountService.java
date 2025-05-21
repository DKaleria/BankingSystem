package by.grgu.accountservice.service;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.dto.AccDto;
import by.grgu.accountservice.dto.AccountDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface AccountService {
    ResponseEntity<Void> createAccount(AccountRequest request);

    ResponseEntity<Account> getAccount(String username);

    ResponseEntity<Void> deleteAccount(String username);

    AccountDTO getAccountData(String username);

    List<AccDto> getAllAccounts();

    void updateAccountStatus(String username, Map<String, String> status);

    AccDto getTotalAccountData(String username);

    boolean updateAccountFields(Map<String, String> updatedData, String token);
   }
