package by.grgu.incomeservice.service;

import by.grgu.incomeservice.database.entity.Income;
import java.math.BigDecimal;
import java.util.List;

public interface IncomeService {
    Income createIncome(Income income);

    List<Income> getAllIncomes(String username);

    BigDecimal getTotalIncomeForMonth(String username, int month, int year);

    List<Income> getIncomesForMonth(String username, int month, int year);

    BigDecimal getTotalIncomeForUser(String username); // ✅ Добавляем метод
}
