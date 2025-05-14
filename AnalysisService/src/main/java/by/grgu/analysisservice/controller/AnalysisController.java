package by.grgu.analysisservice.controller;

import by.grgu.analysisservice.service.AnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping
    public String showAnalysisPage(Model model) {
        System.out.println("📌 Открытие страницы анализа");

        model.addAttribute("username", "—");
        model.addAttribute("balance", "—");
        model.addAttribute("expensePercentage", "—");
        model.addAttribute("month", LocalDate.now().getMonthValue());
        model.addAttribute("year", LocalDate.now().getYear());
        model.addAttribute("expenses", List.of());
        model.addAttribute("incomes", List.of());

        return "analysis";
    }

    @PostMapping("/data")
    public String getAnalysisData(@RequestHeader("username") String username,
                                  @RequestParam int month,
                                  @RequestParam int year,
                                  Model model) {
        System.out.println("📌 Запрос данных анализа, username: " + username);

        try {
            BigDecimal balance = analysisService.getTotalBalance(username, month, year);
            BigDecimal expensePercentage = analysisService.getExpensePercentage(username, month, year);
            List<Object> expenses = analysisService.getExpensesForMonth(username, month, year);
            List<Object> incomes = analysisService.getIncomesForMonth(username, month, year);

            model.addAttribute("username", username);
            model.addAttribute("month", month);
            model.addAttribute("year", year);
            model.addAttribute("balance", balance);
            model.addAttribute("expensePercentage", expensePercentage);
            model.addAttribute("expenses", expenses);
            model.addAttribute("incomes", incomes);

            return "analysis";
        } catch (Exception e) {
            System.err.println("❌ Ошибка в `getAnalysisData`: " + e.getMessage());
            model.addAttribute("errorMessage", "Ошибка при загрузке данных: " + e.getMessage());
            return "analysis"; // Возвращаем страницу с сообщением об ошибке
        }
    }

}


/*
@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // ✅ Загружаем пустую страницу без данных
    @GetMapping
    public String showAnalysisPage(Model model) {
        System.out.println("📌 Открытие страницы анализа");

        model.addAttribute("username", "—"); // 📌 Можно заменить на реальные данные авторизации
        model.addAttribute("balance", "—");
        model.addAttribute("expensePercentage", "—");
        model.addAttribute("month", LocalDate.now().getMonthValue());
        model.addAttribute("year", LocalDate.now().getYear());

        return "analysis"; // Убедитесь, что это просто имя шаблона
    }

    @GetMapping("/test")
    public String testPage() {
        return "test"; // Убедитесь, что возвращаете просто имя шаблона
    }

    // ✅ Загружаем данные анализа при выборе параметров
    @PostMapping("/data")
    public String getAnalysisData(@RequestParam String username,
                                  @RequestParam int month,
                                  @RequestParam int year,
                                  Model model) {
        System.out.println("📌 Запрос данных анализа, username: " + username);

        BigDecimal balance = analysisService.getTotalBalance(username, month, year);
        BigDecimal expensePercentage = analysisService.getExpensePercentage(username, month, year);

        model.addAttribute("username", username);
        model.addAttribute("month", month);
        model.addAttribute("year", year);
        model.addAttribute("balance", balance);
        model.addAttribute("expensePercentage", expensePercentage);

        return "analysis"; // ✅ Загружаем страницу с обновленными данными
    }
}*/
