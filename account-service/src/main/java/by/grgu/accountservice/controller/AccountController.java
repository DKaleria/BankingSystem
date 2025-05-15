package by.grgu.accountservice.controller;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.dto.AccDto;
import by.grgu.accountservice.dto.AccountDTO;
import by.grgu.accountservice.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final RestTemplate restTemplate;

    public AccountController(AccountService accountService, RestTemplate restTemplate) {
        this.accountService = accountService;
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody AccountRequest request) {
        accountService.createAccount(request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("username", request.getUsername());

        System.out.println("📌 Аккаунт создан, передаем username: " + request.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .build();
    }

    @GetMapping("/{username}/birthdate")
    public ResponseEntity<LocalDate> getBirthDate(@PathVariable String username) {
        System.out.println("📌 Запрос на дату рождения аккаунта: " + username);

        Account account = accountService.getAccount(username).getBody();

        if (account == null) {
            System.err.println("❌ Ошибка: аккаунт не найден!");
            return ResponseEntity.notFound().build(); // ✅ Если аккаунта нет, возвращаем 404
        }

        return ResponseEntity.ok(account.getBirthDate()); // ✅ Возвращаем только `birthDate`
    }

    @GetMapping("/{username}")
    public ResponseEntity<Account> getAccount(@PathVariable String username) {
        return accountService.getAccount(username);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String username) {
        return accountService.deleteAccount(username);
    }

    @GetMapping("/account")
    public String showAccount(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос в /account, username: " + username);

        if (username == null || username.isEmpty()) {
            System.err.println("❌ Ошибка: `username` не передан!");
            return "redirect:http://localhost:8082/identity/login";
        }

        // ✅ Загружаем баланс пользователя
        String balanceUrl = "http://localhost:8082/accounts/" + username + "/balance";
        ResponseEntity<BigDecimal> response = restTemplate.getForEntity(balanceUrl, BigDecimal.class);
        BigDecimal totalBalance = response.getBody() != null ? response.getBody() : BigDecimal.ZERO;

        model.addAttribute("username", username);
        model.addAttribute("totalBalance", totalBalance);

        return "account"; // ✅ Загружаем страницу аккаунта с данными
    }


    @GetMapping("/exit")
    public String showExitPage() {
        return "logout";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "http://localhost:8082/identity/login";
    }

    @GetMapping("/{username}/balance")
    @ResponseBody
    public BigDecimal getTotalBalance(@PathVariable String username) {
        System.out.println("📌 Запрос баланса пользователя: " + username);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        try {
            String incomeUrl = "http://localhost:8082/incomes/" + username + "/total";
            ResponseEntity<BigDecimal> incomeResponse = restTemplate.getForEntity(incomeUrl, BigDecimal.class);
            totalIncome = (incomeResponse.getBody() != null) ? incomeResponse.getBody() : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при получении доходов: " + e.getMessage());
        }

        try {
            String expenseUrl = "http://localhost:8082/expenses/" + username + "/total";
            ResponseEntity<BigDecimal> expenseResponse = restTemplate.getForEntity(expenseUrl, BigDecimal.class);
            totalExpense = (expenseResponse.getBody() != null) ? expenseResponse.getBody() : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при получении расходов: " + e.getMessage());
        }

        if (totalExpense.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("🔹 Нет расходов, баланс равен доходам.");
            return totalIncome;
        }

        return totalIncome.subtract(totalExpense);
    }

    @GetMapping("/information")
    public String showAccountPage(@RequestHeader("username") String username, Model model) {
        System.out.println("username in showAccInf: " + username);

        AccountDTO account = accountService.getAccountData(username);
        model.addAttribute("account", account);
        System.out.println("model  in showAccInf: " + model);
        return "account_information"; // ✅ Возвращаем HTML-страницу `account_information.html`
    }

    @PostMapping("/updateField")
    @ResponseBody
    public ResponseEntity<String> updateAccountField(@RequestHeader("username") String username,
                                                     @RequestBody Map<String, String> updatedData) {
        accountService.updateAccountFields(username, updatedData);
        return ResponseEntity.ok("✅ Данные успешно обновлены!");
    }

    @GetMapping
    public ResponseEntity<List<AccDto>> getAllAccounts() {
        List<AccDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
    @PostMapping("/{username}/status")
    public ResponseEntity<Void> updateAccountStatus(@PathVariable String username, @RequestBody Map<String, String> status) {
        accountService.updateAccountStatus(username, status);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{username}/data")
    public ResponseEntity<AccDto> getAccountData(@PathVariable String username) {
        AccDto accountData = accountService.getTotalAccountData(username);
        return ResponseEntity.ok(accountData);
    }
}