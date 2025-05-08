package by.grgu.accountservice.controller;

import by.grgu.accountservice.database.entity.Account;
import by.grgu.accountservice.database.entity.AccountRequest;
import by.grgu.accountservice.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody AccountRequest request) {
        accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
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
    public String showAccount(@SessionAttribute("user") Account account, Model model) {
        model.addAttribute("username", account.getUsername());
        return "account";
    }

    @GetMapping("/accountD")
    public String showDAccount() {
        return "account";
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

}