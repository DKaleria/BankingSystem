package by.grgu.recommendationservice.service.impl;

import by.grgu.recommendationservice.database.entity.RecommendationReport;
import by.grgu.recommendationservice.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final String API_GATEWAY_URL = "http://localhost:8082/";
    private RestTemplate restTemplate;

    @Autowired
    public RecommendationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RecommendationReport generateRecommendations(String username, int month, int year, BigDecimal desiredExpenses) {
        String totalExpenseUrl = API_GATEWAY_URL + "expenses/total?username=" + username + "&month=" + month + "&year=" + year;
        BigDecimal totalExpense = restTemplate.getForObject(totalExpenseUrl, BigDecimal.class);

        String totalIncomeUrl = API_GATEWAY_URL + "incomes/total?username=" + username + "&month=" + month + "&year=" + year;
        BigDecimal totalIncome = restTemplate.getForObject(totalIncomeUrl, BigDecimal.class);

        String expenseSourceUrl = API_GATEWAY_URL + "api/expenses/source-breakdown?username=" + username + "&month=" + month + "&year=" + year;
        ResponseEntity<Map<String, BigDecimal>> sourceResponse = restTemplate.exchange(
                expenseSourceUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, BigDecimal>>() {});
        Map<String, BigDecimal> sourceExpenses = sourceResponse.getBody();

        BigDecimal remainingBudget = totalIncome.subtract(totalExpense);
        BigDecimal difference = remainingBudget.subtract(desiredExpenses);

        List<String> recommendations = new ArrayList<>();

        if (difference.compareTo(BigDecimal.ZERO) < 0) {
            recommendations.add("🚨 Ваши траты превышают доступный бюджет на " + difference.abs() + " руб.");
            recommendations.add("📌 Попробуйте снизить расходы на источники с наибольшими затратами.");
        } else {
            recommendations.add("✅ Отличная финансовая ситуация! У вас осталось " + difference + " руб.");
            recommendations.add("💰 Рекомендуем направить часть средств на инвестиции или накопления.");
        }

        sourceExpenses.forEach((source, amount) -> {
            if (amount.compareTo(totalExpense.multiply(BigDecimal.valueOf(0.3))) > 0) {
                recommendations.add("⚡ Источник `" + source + "` занимает слишком большую долю расходов. Возможно, стоит пересмотреть траты в этом направлении.");
            }
        });

        return new RecommendationReport(totalIncome, totalExpense, desiredExpenses, remainingBudget, recommendations);
    }
}
