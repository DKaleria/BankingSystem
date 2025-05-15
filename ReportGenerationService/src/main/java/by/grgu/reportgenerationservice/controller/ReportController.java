package by.grgu.reportgenerationservice.controller;

import by.grgu.reportgenerationservice.database.entity.MonthlyReport;
import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import by.grgu.reportgenerationservice.service.ReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/generate")
    public String showReportPage(@RequestHeader("username") String username, Model model) {
        BigDecimal totalExpense = reportService.getTotalExpenseForUser(username);
        BigDecimal totalIncome = reportService.getTotalIncomeForUser(username);

        model.addAttribute("username", username);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("totalIncome", totalIncome);

        System.out.println("Model: "+ model);
        return "report";
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

    @PostMapping("/total-expense-report")
    public String generateTotalExpenseReport(@RequestHeader("username") String username,
                                             @RequestParam Map<String, String> params,
                                             Model model) {
        String reportFormat = params.get("reportFormat");

        try {
            String outputPath = reportService.generateTotalExpenseReport(username, reportFormat);
            model.addAttribute("reportMessage", "✅ Отчет успешно создан: " + outputPath);
        } catch (JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "Ошибка при создании отчета.");
        }

        return "report";
    }


    @PostMapping("/monthly-expense")
    public String generateMonthlyExpenseReport(@RequestHeader("username") String username,
                                               @RequestParam Map<String, String> params,
                                               Model model) {
        String reportFormat = params.get("reportFormat");
        int month = Integer.parseInt(params.get("month"));
        int year = Integer.parseInt(params.get("year"));

        try {
            String outputPath = null;
            try {
                outputPath = reportService.generateMonthlyExpenseReport(username, month, year, reportFormat);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            model.addAttribute("reportMessage", "✅ Отчет по расходам за месяц успешно создан: " + outputPath);
        } catch (JRException e) {
            System.err.println("❌ Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "❌ Ошибка при создании отчета.");
        }

        return "report";
    }

    @PostMapping("/monthly-income")
    public String generateMonthlyIncomeReport(@RequestHeader("username") String username,
                                              @RequestParam Map<String, String> params,
                                              Model model) {
        String reportFormat = params.get("reportFormat");
        int month = Integer.parseInt(params.get("month"));
        int year = Integer.parseInt(params.get("year"));

        try {
            String outputPath = reportService.generateMonthlyIncomeReport(username, month, year, reportFormat);
            model.addAttribute("reportMessage", "✅ Отчет по доходам за месяц успешно создан: " + outputPath);
        } catch (IOException | JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "❌ Ошибка при создании отчета.");
        }

        return "report";
    }

    @PostMapping("/total-report")
    public String generateTotalReport(@RequestHeader("username") String username,
                                      @RequestParam Map<String, String> params,
                                      Model model) {
        String reportFormat = params.get("reportFormat");
        int month = Integer.parseInt(params.get("month"));
        int year = Integer.parseInt(params.get("year"));

        try {
            String outputPath = reportService.generateTotalReport(username, month, year, reportFormat);
            model.addAttribute("reportMessage", "✅ Общий финансовый отчет успешно создан: " + outputPath);
        } catch (IOException | JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "❌ Ошибка при создании общего отчета.");
        }

        return "report";
    }
}