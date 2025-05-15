package by.grgu.recommendationservice.service;

import by.grgu.recommendationservice.database.entity.RecommendationReport;

import java.math.BigDecimal;

public interface RecommendationService {
    RecommendationReport generateRecommendations
            (String username,
                                                 int month, int year, BigDecimal desiredExpenses);
}
