package by.grgu.reportgenerationservice.controller;

import by.grgu.reportgenerationservice.database.entity.MonthlyReport;
import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import by.grgu.reportgenerationservice.service.ReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
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

        System.out.println("Model: " + model);
        return "report";
    }

    @PostMapping("/income-by-source")
    public ResponseEntity<String> generateIncomeBySourceReport(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String selectedSource,
            @RequestParam String format) {

        try {
            String reportPath = reportService.generateIncomeBySourceReport(username, format, month, year, selectedSource);
            return ResponseEntity.ok(reportPath);
        } catch (JRException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании отчета: " + e.getMessage());
        }
    }

    @GetMapping("/income-sources")
    public ResponseEntity<List<String>> getIncomeSources(@RequestHeader("username") String username) {
        // ✅ Теперь вызываем метод `getIncomeSources()` из `ReportService`, а не `restTemplate`
        List<String> sources = reportService.getIncomeSources(username);
        return ResponseEntity.ok(sources);
    }

    @PostMapping("/expense-by-description")
    public ResponseEntity<String> generateExpenseByDescriptionReport(
            @RequestHeader("username") String username,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String description,
            @RequestParam String format) {

        try {
            String reportPath = reportService.generateExpenseByDescriptionReport(username, format, month, year, description);
            return ResponseEntity.ok(reportPath);
        } catch (JRException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании отчета: " + e.getMessage());
        }
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
            // Проверка на наличие параметра reportFormat
            if (reportFormat == null || reportFormat.isEmpty()) {
                model.addAttribute("reportMessage", "❌ Формат отчета не указан.");
                return "report";
            }

            String outputPath = reportService.generateTotalExpenseReport(username, reportFormat);
            model.addAttribute("reportMessage", "✅ Отчет успешно создан: " + outputPath);
        } catch (JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "Ошибка при создании отчета.");
        }

        return "report";
    }

    @PostMapping("/create")
    public String createReport(@RequestHeader("username") String username,
                               @RequestParam Map<String, String> params,
                               Model model) {
        String reportType = params.get("reportType");
        String reportFormat = params.get("reportFormat");
        String outputPath = "";

        try {
            switch (reportType) {
                case "total-expense-report":
                    outputPath = reportService.generateTotalExpenseReport(username, reportFormat);
                    break;
                case "monthly-expense":
                    int month = Integer.parseInt(params.get("month"));
                    int year = Integer.parseInt(params.get("year"));
                    outputPath = reportService.generateMonthlyExpenseReport(username, month, year, reportFormat);
                    break;
                case "monthly-income":
                    month = Integer.parseInt(params.get("month"));
                    year = Integer.parseInt(params.get("year"));
                    outputPath = reportService.generateMonthlyIncomeReport(username, month, year, reportFormat);
                    break;
                case "total-report":
                    month = Integer.parseInt(params.get("month"));
                    year = Integer.parseInt(params.get("year"));
                    outputPath = reportService.generateTotalReport(username, month, year, reportFormat);
                    break;
                case "income-by-source": // ✅ Теперь поддерживается "Доходы по источнику"
                    month = Integer.parseInt(params.get("month"));
                    year = Integer.parseInt(params.get("year"));
                    String source = params.get("source"); // ✅ Добавляем источник
                    outputPath = reportService.generateIncomeBySourceReport(username, reportFormat, month, year, source);
                    break;
                case "expense-by-description": // ✅ Теперь поддерживается "Расходы по описанию"
                    month = Integer.parseInt(params.get("month"));
                    year = Integer.parseInt(params.get("year"));
                    String description = params.get("description"); // ✅ Извлекаем описание расхода
                    outputPath = reportService.generateExpenseByDescriptionReport(username, reportFormat, month, year, description);
                    break;
                default:
                    model.addAttribute("reportMessage", "❌ Неверный тип отчета");
                    return "report";
            }

            model.addAttribute("reportMessage", "✅ Отчет успешно создан.");
            model.addAttribute("reportPath", outputPath);
            model.addAttribute("reportFormat", reportFormat);
        } catch (Exception e) {
            model.addAttribute("reportMessage", "❌ Ошибка при создании отчета: " + e.getMessage());
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

    @GetMapping("/view")
    public ResponseEntity<Resource> viewReport(@RequestParam String path, @RequestParam String format) {
        File file = new File(path);
        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            MediaType mediaType;

            switch (format) {
                case "pdf":
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "text":
                    mediaType = MediaType.TEXT_PLAIN;
                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName()) // ✅ Файл будет скачиваться
                            .body(resource);
                case "png":
                    mediaType = MediaType.IMAGE_PNG;
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
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
            model.addAttribute("reportMessage", "✅ Отчет по доходам за месяц успешно создан.");
            model.addAttribute("reportPath", outputPath); // Добавляем путь к отчету
        } catch (IOException | JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("reportMessage", "❌ Ошибка при создании отчета.");
        }

        return "report"; // Убедитесь, что это имя вашего шаблона
    }

    @PostMapping("/total-report")
    public String generateTotalReport(@RequestHeader("username") String username,
                                      @RequestParam Map<String, String> params,
                                      Model model) {
        String reportFormat = params.get("reportFormat");
        String monthStr = params.get("month");
        String yearStr = params.get("year");

        // Проверка на пустые значения
        if (monthStr == null || monthStr.isEmpty() || yearStr == null || yearStr.isEmpty()) {
            model.addAttribute("errorMessage", "❌ Месяц и год должны быть указаны.");
            return "report";
        }

        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        try {
            String outputPath = reportService.generateTotalReport(username, month, year, reportFormat);
            model.addAttribute("reportLink", "/reports/download?path=" + outputPath);
        } catch (IOException | JRException e) {
            System.err.println("Ошибка генерации отчета: " + e.getMessage());
            model.addAttribute("errorMessage", "❌ Ошибка при создании отчета.");
        }

        return "report";
    }
}