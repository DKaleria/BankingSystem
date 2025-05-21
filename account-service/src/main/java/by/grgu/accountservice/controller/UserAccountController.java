package by.grgu.accountservice.controller;

import by.grgu.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user-accounts")
public class UserAccountController {
    private final AccountService accountService;

    public UserAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/updateField")
    @ResponseBody  // ✅ Гарантируем возврат JSON
    public ResponseEntity<Map<String, String>> updateAccountField(@RequestBody Map<String, String> updatedData,
                                                                  @RequestHeader("Authorization") String token) {
        System.out.println("🔄 Данные, переданные в AccountService: " + updatedData);

        boolean accountUpdateSuccess = accountService.updateAccountFields(updatedData, token);

        Map<String, String> response = new HashMap<>();
        response.put("status", accountUpdateSuccess ? "success" : "error");
        response.put("message", accountUpdateSuccess ? "✅ Данные успешно обновлены в AccountService!" : "❌ Ошибка обновления данных!");

        return ResponseEntity.ok(response);
    }

}
