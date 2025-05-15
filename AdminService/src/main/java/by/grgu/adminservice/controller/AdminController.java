package by.grgu.adminservice.controller;

import by.grgu.adminservice.database.entity.AccountRequest;
import by.grgu.adminservice.dto.AccountDTO;
import by.grgu.adminservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RestTemplate restTemplate;

    @Autowired
    public AdminController(AdminService adminService, RestTemplate restTemplate) {
        this.adminService = adminService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/users")
    public String getAllAccounts(Model model) {
        List<AccountDTO> users = adminService.getAllAccounts();
        model.addAttribute("users", users);
        return "admin-dashboard"; // ✅ Загружаем список пользователей
    }

    // ✅ Редактирование информации о пользователе
    @GetMapping("/users/{username}")
    public String getAccountData(@PathVariable String username, Model model) {
        AccountDTO account = adminService.getAccountData(username);
        model.addAttribute("account", account);
        return "admin-user-edit"; // ✅ Название HTML-шаблона исправлено
    }

    // ✅ Обновление данных пользователя
    @PostMapping("/users/update")
    public String updateAccount(@RequestParam String username, @RequestParam Map<String, String> updates) {
        adminService.updateAccountStatus(username, updates);
        return "redirect:/admin/users"; // ✅ Перенаправление обратно к списку пользователей
    }

}