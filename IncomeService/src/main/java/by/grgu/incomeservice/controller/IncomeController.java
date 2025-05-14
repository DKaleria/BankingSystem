package by.grgu.incomeservice.controller;

import by.grgu.incomeservice.database.entity.Income;
import by.grgu.incomeservice.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @Autowired
    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping("/incomes")
    public String getIncomes(@RequestHeader("username") String username,
                             @RequestParam(value = "incomeType", defaultValue = "all") String incomeType,
                             @RequestParam(value = "month", required = false) Integer month,
                             @RequestParam(value = "year", required = false) Integer year,
                             Model model) {
        System.out.println("📌 Запрос в /incomes от API Gateway, username: " + username);

        model.addAttribute("username", username);
        model.addAttribute("incomeType", incomeType);
        model.addAttribute("month", month);
        model.addAttribute("year", year);

        List<Income> incomes;
        if ("monthly".equals(incomeType) && month != null && year != null) {
            incomes = incomeService.getIncomesForMonth(username, month, year);
        } else {
            incomes = incomeService.getAllIncomes(username);
        }

        model.addAttribute("incomes", incomes);
        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "income";
    }

    @GetMapping("/monthly")
    public String getMonthlyIncomes(@RequestHeader("username") String username,
                                    @RequestParam int month,
                                    @RequestParam int year,
                                    Model model) {
        System.out.println("📌 Запрос на доходы за месяц, username: " + username);

        List<Income> incomes = incomeService.getIncomesForMonth(username, month, year);

        model.addAttribute("username", username);
        model.addAttribute("month", month);
        model.addAttribute("year", year);
        model.addAttribute("incomes", incomes);

        return "income";
    }

    @PostMapping("/add-income")
    public String addIncome(@RequestHeader("username") String username, @ModelAttribute Income income, Model model) {
        System.out.println("📌 Запрос на добавление дохода, username: " + username);

        income.setUsername(username);

        try {
            incomeService.createIncome(income);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage()); // ✅ Передаем ошибку в Thymeleaf
            return getUserIncomes(username, model); // ✅ Возвращаем страницу доходов с предупреждением
        }

        return getUserIncomes(username, model); // ✅ Перенаправляем на страницу доходов
    }



    @GetMapping("/user-incomes")
    public String getUserIncomes(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос в /user-incomes от API Gateway, username: " + username);

        List<Income> incomes = incomeService.getAllIncomes(username);

        if (incomes == null || incomes.isEmpty()) {
            System.out.println("⚠️ У пользователя пока нет доходов, передаем пустой список.");
            incomes = new ArrayList<>();
        }
        System.out.println("username: "+ username);
        System.out.println("incomes: "+ incomes);

        model.addAttribute("username", username);
        model.addAttribute("incomes", incomes);

        return "income";
    }


    @GetMapping("/total")
    @ResponseBody
    public BigDecimal getTotalIncome(@RequestHeader("username") String username,
                                     @RequestParam int month,
                                     @RequestParam int year) {
        System.out.println("📌 Запрос на total от API Gateway, username: " + username);
        return incomeService.getTotalIncomeForMonth(username, month, year);
    }

    @GetMapping("/{username}/total")
    @ResponseBody
    public BigDecimal getTotalIncomeForUser(@PathVariable String username) {
        System.out.println("📌 Запрос общей суммы доходов пользователя: " + username);
        return incomeService.getTotalIncomeForUser(username);
    }


    @GetMapping("/all")
    @ResponseBody
    public List<Income> getAllIncomes(@RequestHeader("username") String username) {
        System.out.println("📌 Запрос на all от API Gateway, username: " + username);
        return incomeService.getAllIncomes(username);
    }

    @GetMapping("/show")
    public String showIncome(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос в /show от API Gateway, username: " + username);
        model.addAttribute("username", username);
        return "income";
    }

    @GetMapping("/{username}/all")
    @ResponseBody
    public List<Income> getAllIncomesForUser(@PathVariable String username) {
        System.out.println("📌 Запрос всех доходов пользователя: " + username);

        List<Income> incomes = incomeService.getAllIncomes(username);

        if (incomes.isEmpty()) {
            System.out.println("⚠️ У пользователя пока нет доходов, передаем пустой список.");
        }

        return incomes;
    }
}
