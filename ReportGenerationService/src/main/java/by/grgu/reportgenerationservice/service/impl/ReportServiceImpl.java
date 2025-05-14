package by.grgu.reportgenerationservice.service.impl;

import by.grgu.reportgenerationservice.database.entity.MonthlyReport;
import by.grgu.reportgenerationservice.database.entity.TotalReport;
import by.grgu.reportgenerationservice.database.repository.MonthlyReportRepository;
import by.grgu.reportgenerationservice.database.repository.TotalReportRepository;
import by.grgu.reportgenerationservice.dto.ExpenseDTO;
import by.grgu.reportgenerationservice.dto.IncomeDTO;
import by.grgu.reportgenerationservice.dto.MonthlyReportDTO;
import by.grgu.reportgenerationservice.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final RestTemplate restTemplate;
    private final MonthlyReportRepository monthlyReportRepository;
    private final TotalReportRepository totalReportRepository;
    private static final String API_GATEWAY_URL = "http://localhost:8082/";

    @Autowired
    public ReportServiceImpl(RestTemplate restTemplate,
                             MonthlyReportRepository monthlyReportRepository,
                             TotalReportRepository totalReportRepository) {
        this.restTemplate = restTemplate;
        this.monthlyReportRepository = monthlyReportRepository;
        this.totalReportRepository = totalReportRepository;
    }

    @Override
    public BigDecimal getTotalExpenseForMonth(String username, int month, int year) {
        String url = API_GATEWAY_URL + "expenses/total?username="
                + username + "&month=" + month + "&year=" + year;
        return restTemplate.getForObject(url, BigDecimal.class);
    }

    @Override
    public BigDecimal getTotalExpenseForUser(String username) {
        String url = API_GATEWAY_URL + "expenses/" + username + "/total";
        return restTemplate.getForObject(url, BigDecimal.class);
    }

    @Override
    public BigDecimal getTotalIncomeForMonth(String username, int month, int year) {
        String url = API_GATEWAY_URL +
                "incomes/total?username=" + username + "&month=" + month + "&year=" + year;
        return restTemplate.getForObject(url, BigDecimal.class);
    }

    @Override
    public BigDecimal getTotalIncomeForUser(String username) {
        String url = API_GATEWAY_URL + "incomes/" + username + "/total";
        return restTemplate.getForObject(url, BigDecimal.class);
    }


    @Override
    public void saveMonthlyReport(String username, int month, int year) {
        BigDecimal totalIncome = getTotalIncomeForMonth(username, month, year);
        BigDecimal totalExpense = getTotalExpenseForMonth(username, month, year);

        MonthlyReport report = new MonthlyReport(username,
                month, year, totalIncome, totalExpense);

        monthlyReportRepository.save(report);
    }

    @Override
    public void saveTotalReport(String username) {
        BigDecimal totalIncome = getTotalIncomeForUser(username);
        BigDecimal totalExpense = getTotalExpenseForUser(username);

        TotalReport totalReport = new TotalReport(username,
                totalIncome, totalExpense);

        totalReportRepository.save(totalReport);
    }

    private List<IncomeDTO> getIncomesForMonth(String username, int month, int year) {
        String url = String.format(
                "%s/incomes/monthly?username=%s&month=%d&year=%d",
                API_GATEWAY_URL, username, month, year);
        ResponseEntity<List<IncomeDTO>> responseEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        });
        return responseEntity.getBody();
    }

    private List<ExpenseDTO> getExpensesForMonth(String username, int month, int year) {
        String url = String.format("%s/expenses/monthly?username=%s&month=%d&year=%d",
                API_GATEWAY_URL, username, month, year);
        ResponseEntity<List<ExpenseDTO>> responseEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        });
        return responseEntity.getBody();
    }

    public MonthlyReportDTO generateMonthlyReport(String username, int month, int year) {
        BigDecimal totalIncome = getTotalIncomeForMonth(username, month, year);
        BigDecimal totalExpense = getTotalExpenseForMonth(username, month, year);

        List<IncomeDTO> incomes = getIncomesForMonth(username, month, year);
        List<ExpenseDTO> expenses = getExpensesForMonth(username, month, year);

        MonthlyReportDTO report = new MonthlyReportDTO(username,
                month, year, totalIncome, totalExpense,
                incomes, expenses);

        return report;
    }


    public void generateTotalExpenseReport(String username) throws JRException {
        System.out.println("📌 Генерация общего отчета по расходам и доходам для пользователя: " + username);

        // 📌 Устанавливаем путь к JRXML-файлу
        JasperReport jasperReport = JasperCompileManager.compileReport("/home/valeryia/JaspersoftWorkspace/MyReports/total_expense_report.jrxml");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);

        BigDecimal totalExpense = getTotalExpenseForUser(username);
        BigDecimal totalIncome = getTotalIncomeForUser(username);

        parameters.put("totalIncome", totalIncome);
        parameters.put("totalExpense", totalExpense);

        List<Map<String, Object>> reportData = new ArrayList<>();

        Map<String, Object> row = new HashMap<>();
        row.put("description", "Общий доход");
        row.put("amount", totalIncome);
        reportData.add(row);

        row = new HashMap<>();
        row.put("description", "Общий расход");
        row.put("amount", totalExpense);
        reportData.add(row);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 📌 Обновляем путь для сохранения PDF-файла
        String outputPath = "/home/valeryia/JaspersoftWorkspace/MyReports/total_expense_report.pdf";
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        System.out.println("✅ Общий отчет по расходам создан: " + outputPath);
    }


}