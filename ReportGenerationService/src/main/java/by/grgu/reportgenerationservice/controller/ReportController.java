package by.grgu.reportgenerationservice.controller;

import by.grgu.reportgenerationservice.database.entity.MonthlyReport;
import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import by.grgu.reportgenerationservice.service.ReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/generate")
    public String showReportPage(@RequestHeader("username") String username, Model model) {
        System.out.println("📌 Загрузка страницы отчетов, username: " + username);

        BigDecimal totalExpense = reportService.getTotalExpenseForUser(username);
        BigDecimal totalIncome = reportService.getTotalIncomeForUser(username);

        model.addAttribute("username", username);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("totalIncome", totalIncome);

        return "report"; // ✅ Отображаем страницу report.html
    }

    @GetMapping("/total-monthly-expense")
    public ResponseEntity<BigDecimal> getMonthlyExpense(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year) {

        BigDecimal totalExpense = reportService.getTotalExpenseForMonth(username, month, year);
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/total-expense")
    public ResponseEntity<BigDecimal> getTotalExpense(
            @RequestHeader("username") String username) {

        BigDecimal totalExpense = reportService.getTotalExpenseForUser(username);
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/total-monthly-income")
    public ResponseEntity<BigDecimal> getMonthlyIncome(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year) {

        BigDecimal totalIncome = reportService.getTotalIncomeForMonth(username, month, year);
        return ResponseEntity.ok(totalIncome);
    }

    @GetMapping("/total-income")
    public ResponseEntity<BigDecimal> getTotalIncome(
            @RequestHeader("username") String username) {

        BigDecimal totalIncome = reportService.getTotalIncomeForUser(username);
        return ResponseEntity.ok(totalIncome);
    }

    @GetMapping("/total-monthly-report")
    public ResponseEntity<MonthlyReport> getTotalMonthlyReport(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year) {

        reportService.saveMonthlyReport(username, month, year);

        MonthlyReport report = new MonthlyReport(username, month, year,
                reportService.getTotalIncomeForMonth(username, month, year),
                reportService.getTotalExpenseForMonth(username, month, year));
        return ResponseEntity.ok(report);
    }

    @GetMapping("/total-report")
    public ResponseEntity<MonthlyReport> getTotalReport(
            @RequestHeader("username") String username) {

        reportService.saveTotalReport(username);

        MonthlyReport totalReport = new MonthlyReport(username, 0, 0,
                reportService.getTotalIncomeForUser(username),
                reportService.getTotalExpenseForUser(username));
        return ResponseEntity.ok(totalReport);
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<MonthlyReportDTO> getMonthlyReport(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year) {

        MonthlyReportDTO report = reportService.generateMonthlyReport(username, month, year);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/total-expense-report")
    public ResponseEntity<String> generateTotalExpenseReport(@RequestHeader("username") String username) {
        System.out.println("📌 Запрос на генерацию общего отчета по расходам и доходам, username: " + username);

        try {
            reportService.generateTotalExpenseReport(username);
            return ResponseEntity.ok("✅ Отчет успешно создан!");
        } catch (JRException e) {
            System.err.println("❌ Ошибка генерации отчета: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Ошибка при создании отчета.");
        }
    }
}