package by.grgu.analysisservice.service.impl;

import by.grgu.analysisservice.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final RestTemplate restTemplate;

    private static final String INCOME_URL_TEMPLATE = "http://localhost:8082/incomes/%s/all?month=%d&year=%d";
    private static final String EXPENSE_URL_TEMPLATE = "http://localhost:8082/expenses/%s/all?month=%d&year=%d";

    public AnalysisServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getTotalBalance(String username, int month, int year) {
        try {
            String incomeUrl = String.format("http://localhost:8082/incomes/%s/total?month=%d&year=%d", username, month, year);
            BigDecimal totalIncome = getResponse(incomeUrl);

            String expenseUrl = String.format("http://localhost:8082/expenses/%s/total?month=%d&year=%d", username, month, year);
            BigDecimal totalExpense = getResponse(expenseUrl);

            return totalIncome.subtract(totalExpense);
        } catch (Exception e) {
            System.err.println("Ошибка в `getTotalBalance`: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }


    @Override
    public BigDecimal getExpensePercentage(String username, int month, int year) {
        BigDecimal totalIncome = getTotalIncome(username, month, year);
        BigDecimal totalExpense = getTotalExpense(username, month, year);

        if (totalIncome.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return totalExpense.multiply(BigDecimal.valueOf(100)).divide(totalIncome, 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public List<Object> getExpensesForMonth(String username, int month, int year) {
        String expenseUrl = String.format(EXPENSE_URL_TEMPLATE, username, month, year);
        ResponseEntity<Object[]> response = restTemplate.getForEntity(expenseUrl, Object[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
    }

    @Override
    public List<Object> getIncomesForMonth(String username, int month, int year) {
        String incomeUrl = String.format(INCOME_URL_TEMPLATE, username, month, year);
        ResponseEntity<Object[]> response = restTemplate.getForEntity(incomeUrl, Object[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
    }

    private BigDecimal getTotalIncome(String username, int month, int year) {
        String incomeUrl = String.format("http://localhost:8082/incomes/%s/total?month=%d&year=%d", username, month, year);
        return getResponse(incomeUrl);
    }

    private BigDecimal getTotalExpense(String username, int month, int year) {
        String expenseUrl = String.format("http://localhost:8082/expenses/%s/total?month=%d&year=%d", username, month, year);
        return getResponse(expenseUrl);
    }

    private BigDecimal getResponse(String url) {
        try {
            ResponseEntity<BigDecimal> response = restTemplate.getForEntity(url, BigDecimal.class);
            return response.getBody() != null ? response.getBody() : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Ошибка при запросе: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}