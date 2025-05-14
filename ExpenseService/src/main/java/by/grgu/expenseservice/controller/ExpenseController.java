package by.grgu.expenseservice.controller;

import by.grgu.expenseservice.database.entity.Expense;
import by.grgu.expenseservice.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/expenses")
    public String getExpenses(@RequestHeader("username") String username,
                              @RequestParam(value = "expenseType", defaultValue = "all") String expenseType,
                              @RequestParam(value = "month", required = false) Integer month,
                              @RequestParam(value = "year", required = false) Integer year,
                              Model model) {
        System.out.println("📌 Запрос в /expenses, username: " + username);

        model.addAttribute("username", username);
        model.addAttribute("expenseType", expenseType);
        model.addAttribute("month", month);
        model.addAttribute("year", year);

        List<Expense> expenses;
        if ("monthly".equals(expenseType) && (month == null || year == null)) {
            System.err.println("⚠️ Ошибка: месяц и год не указаны!");
            model.addAttribute("errorMessage", "❌ Выберите месяц и год!");
            expenses = new ArrayList<>(); // ✅ Отправляем пустой список
        } else if ("monthly".equals(expenseType)) {
            expenses = expenseService.getExpensesForMonth(username, month, year);
        } else {
            expenses = expenseService.getAllExpenses(username);
        }

        model.addAttribute("expenses", expenses);
        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "expense";
    }

    @GetMapping("/monthly")
    public String getMonthlyExpenses(@RequestHeader("username") String username,
                                     @RequestParam(required = false) Integer month,
                                     @RequestParam(required = false) Integer year,
                                     Model model) {
        System.out.println("📌 Запрос на расходы за месяц, username: " + username);

        if (month == null || year == null) {
            System.err.println("❌ Ошибка: `month` или `year` не переданы!");
            model.addAttribute("errorMessage", "Выберите месяц и год!");
            return "expense"; // ✅ Возвращаем страницу с предупреждением
        }

        List<Expense> expenses = expenseService.getExpensesForMonth(username, month, year);

        model.addAttribute("username", username);
        model.addAttribute("month", month);
        model.addAttribute("year", year);
        model.addAttribute("expenses", expenses);

        return "expense";
    }

    @PostMapping("/add-expense")
    public String addExpense(@RequestHeader("username") String username, @ModelAttribute Expense expense, Model model) {
        System.out.println("📌 Запрос на добавление расхода, username: " + username);

        expense.setUsername(username);

        try {
            expenseService.createExpense(expense);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage()); // ✅ Передаем ошибку в Thymeleaf
            return getUserExpenses(username, model); // ✅ Возвращаем страницу расходов с предупреждением
        }

        return getUserExpenses(username, model); // ✅ Перенаправляем на страницу расходов
    }

    @GetMapping("/user-expenses")
    public String getUserExpenses(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос в /user-expenses от API Gateway, username: " + username);

        List<Expense> expenses = expenseService.getAllExpenses(username);

        if (expenses == null || expenses.isEmpty()) {
            System.out.println("⚠️ У пользователя пока нет расходов, передаем пустой список.");
            expenses = new ArrayList<>();
        }

        System.out.println("username: " + username);
        System.out.println("expenses: " + expenses);

        model.addAttribute("username", username);
        model.addAttribute("expenses", expenses);

        return "expense";
    }

    @GetMapping("/total")
    @ResponseBody
    public BigDecimal getTotalExpense(@RequestHeader("username") String username,
                                      @RequestParam int month,
                                      @RequestParam int year) {
        System.out.println("📌 Запрос на total от API Gateway, username: " + username);
        return expenseService.getTotalExpenseForMonth(username, month, year);
    }

    @GetMapping("/{username}/total")
    @ResponseBody
    public BigDecimal getTotalExpenseForUser(@PathVariable String username) {
        System.out.println("📌 Запрос общей суммы расходов пользователя: " + username);

        List<Expense> expenses = expenseService.getAllExpenses(username);

        BigDecimal totalExpense = expenses.stream()
                .map(expense -> BigDecimal.valueOf(expense.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalExpense;
    }


    @GetMapping("/all")
    public String getAllExpenses(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос всех расходов, username: " + username);

        List<Expense> expenses = expenseService.getAllExpenses(username);

        model.addAttribute("username", username);
        model.addAttribute("expenses", expenses);

        return "expense"; // ✅ Показываем страницу со всеми расходами
    }


    @GetMapping("/show")
    public String showExpense(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Запрос в /show от API Gateway, username: " + username);
        model.addAttribute("username", username);
        return "expense";
    }

    @GetMapping("/{username}/all")
    @ResponseBody
    public List<Expense> getAllExpensesForUser(@PathVariable String username) {
        System.out.println("📌 Запрос всех расходов пользователя: " + username);

        List<Expense> expenses = expenseService.getAllExpenses(username);

        if (expenses.isEmpty()) {
            System.out.println("⚠️ У пользователя пока нет расходов, передаем пустой список.");
        }

        return expenses;
    }

}
