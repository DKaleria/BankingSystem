package by.grgu.adminservice.service;

import by.grgu.adminservice.dto.AccountDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface AdminService {
    List<AccountDTO> getAllAccounts();

    ResponseEntity<Void> updateAccountStatus(String username, Map<String, String> status);

    AccountDTO getAccountData(String username);
}
