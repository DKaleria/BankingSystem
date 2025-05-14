package by.grgu.analysisservice.service;

import java.math.BigDecimal;
import java.util.List;

public interface AnalysisService {

    BigDecimal getTotalBalance(String username, int month, int year);

    BigDecimal getExpensePercentage(String username, int month, int year);

    List<Object> getExpensesForMonth(String username, int month, int year);

    List<Object> getIncomesForMonth(String username, int month, int year);
}