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
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
        String url = String.format("%s/api/incomes/monthly?username=%s&month=%d&year=%d",
                API_GATEWAY_URL, username, month, year);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<List<IncomeDTO>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<IncomeDTO>>() {}
        );

        List<IncomeDTO> incomes = responseEntity.getBody();

        for (IncomeDTO income : incomes) {
            if (income.getSource() == null) {
                income.setSource("Не указан");
            }
        }

        return incomes;
    }

    private List<ExpenseDTO> getExpensesForMonth(String username, int month, int year) {
        String url = String.format("%s/api/expenses/monthly?username=%s&month=%d&year=%d",
                API_GATEWAY_URL, username, month, year);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ExpenseDTO>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ExpenseDTO>>() {}
        );

        List<ExpenseDTO> expenses = responseEntity.getBody();

        for (ExpenseDTO expense : expenses) {
            if (expense.getDescription() == null) {
                expense.setDescription("Нет описания");
            }
        }

        return expenses;
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

    @Override
    public String generateTotalExpenseReport(String username, String format) throws JRException {
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

        try {
            return exportReport(jasperPrint, format, "total_expense_report");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String exportReport(JasperPrint jasperPrint, String format, String reportName) throws JRException, IOException {
        String outputPath = "/home/valeryia/JaspersoftWorkspace/MyReports/" + reportName + "." + format;

        switch (format.toLowerCase()) {
            case "pdf":
                JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
                break;
            case "png":
                Image img = JasperPrintManager.printPageToImage(jasperPrint, 0, 1.0f);
                BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(img, 0, 0, null);
                g2d.dispose();
                ImageIO.write(bufferedImage, "png", new File(outputPath));
                break;
            case "text":
                String textOutputPath = outputPath.replace(".text", ".txt");  // ✅ Используем `.txt`, чтобы избежать белого экрана

                if (jasperPrint.getPages().isEmpty()) {
                    throw new RuntimeException("❌ Ошибка: В отчёте нет данных!");
                }

                JRTextExporter textExporter = new JRTextExporter();
                textExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                textExporter.setExporterOutput(new SimpleWriterExporterOutput(textOutputPath));

                SimpleTextReportConfiguration textConfig = new SimpleTextReportConfiguration();
                textConfig.setCharWidth(10f);
                textConfig.setPageWidthInChars(200);
                textConfig.setPageHeightInChars(50);
                textExporter.setConfiguration(textConfig);

                textExporter.exportReport();

                // ✅ Проверяем, записан ли текст в файл
                if (Files.size(Paths.get(textOutputPath)) == 0) {
                    throw new RuntimeException("❌ Ошибка: Файл отчёта пуст!");
                }

                return textOutputPath;


            default:
                throw new IllegalArgumentException("Неподдерживаемый формат отчета: " + format);
        }
        return outputPath;
    }

    @Override
    public String generateMonthlyExpenseReport(String username, int month, int year, String format) throws JRException, IOException {
        JasperReport jasperReport = JasperCompileManager.compileReport("/home/valeryia/JaspersoftWorkspace/MyReports/monthly_expense_report.jrxml");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("month", month);
        parameters.put("year", year);

        BigDecimal totalExpense = getTotalExpenseForMonth(username, month, year);
        parameters.put("totalExpense", totalExpense);

        List<ExpenseDTO> expenses = getExpensesForMonth(username, month, year);

        for (ExpenseDTO expense : expenses) {
            if (expense.getDescription() == null) {
                System.out.println("Ошибка: В списке расходов есть объект без `description`!");
                expense.setDescription("Нет описания");
            }
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(expenses);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return exportReport(jasperPrint, format, "monthly_expense_report");
    }

    @Override
    public String generateMonthlyIncomeReport(String username, int month, int year, String format)
            throws JRException, IOException {
        JasperReport jasperReport = JasperCompileManager.compileReport("/home/valeryia/JaspersoftWorkspace/MyReports/monthly_income_report.jrxml");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("month", month);
        parameters.put("year", year);

        BigDecimal totalIncome = getTotalIncomeForMonth(username, month, year);
        parameters.put("totalIncome", totalIncome);

        List<IncomeDTO> incomes = getIncomesForMonth(username, month, year);

        for (IncomeDTO income : incomes) {
            if (income.getSource() == null) {
                income.setSource("Не указан");
            }
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(incomes);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return exportReport(jasperPrint, format, "monthly_income_report");
    }

    @Override
    public String generateTotalReport(String username, int month, int year, String format)
            throws JRException, IOException {
        JasperReport jasperReport = JasperCompileManager.compileReport("/home/valeryia/JaspersoftWorkspace/MyReports/total_report.jrxml");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", new String(username.getBytes(), Charset.forName("UTF-8")));
        parameters.put("month", month);
        parameters.put("year", year);

        BigDecimal totalIncome = getTotalIncomeForMonth(username, month, year);
        BigDecimal totalExpense = getTotalExpenseForMonth(username, month, year);
        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        parameters.put("totalIncome", totalIncome);
        parameters.put("totalExpense", totalExpense);
        parameters.put("netSavings", netSavings);


        List<IncomeDTO> incomes = getIncomesForMonth(username, month, year);
        List<ExpenseDTO> expenses = getExpensesForMonth(username, month, year);

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (IncomeDTO income : incomes) {
            Map<String, Object> row = new HashMap<>();
            row.put("type", "Доход");
            row.put("source", income.getSource());
            row.put("amount", income.getAmount());
            reportData.add(row);
        }

        for (ExpenseDTO expense : expenses) {
            Map<String, Object> row = new HashMap<>();
            row.put("type", "Расход");
            row.put("source", expense.getDescription());
            row.put("amount", expense.getAmount());
            reportData.add(row);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return exportReport(jasperPrint, format, "total_report");
    }

    @Override
    public List<String> getIncomeSources(String username) {
        String url = API_GATEWAY_URL + "api/incomes/sources"; // ✅ Запрос через API Gateway
        return Arrays.asList(restTemplate.getForObject(url, String[].class));
    }

    @Override
    public String generateIncomeBySourceReport(
            String username, String format, int month, int year, String selectedSource)
            throws JRException, IOException {

        JasperReport jasperReport = JasperCompileManager.compileReport("/home/valeryia/JaspersoftWorkspace/MyReports/income_by_source.jrxml");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("month", month);
        parameters.put("year", year);
        parameters.put("selectedSource", selectedSource); // ✅ Указанный источник дохода

        // ✅ Запрос данных через API Gateway
        String url = API_GATEWAY_URL + "incomes/" + username + "?month=" + month + "&year=" + year;
        List<IncomeDTO> incomes = Arrays.asList(restTemplate.getForObject(url, IncomeDTO[].class));

        // ✅ Фильтруем данные только по `selectedSource`
        List<Map<String, Object>> filteredData = incomes.stream()
                .filter(income -> income.getSource().equals(selectedSource))
                .map(income -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("source", income.getSource());
                    row.put("amount", income.getAmount());
                    return row;
                })
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(filteredData);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // ✅ Экспорт отчета в нужный формат
        return exportReport(jasperPrint, format, "income_by_source");
    }

}