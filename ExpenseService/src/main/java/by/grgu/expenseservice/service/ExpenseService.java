package by.grgu.expenseservice.service;

import by.grgu.expenseservice.database.entity.Expense;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    Expense createExpense(Expense expense);

    List<Expense> getAllExpenses(String username);

    BigDecimal getTotalExpenseForMonth(String username, int month, int year);

    Map<String, BigDecimal> getExpenseBreakdown(String username, int month, int year);

    List<Expense> getExpensesForMonth(String username, int month, int year);
}
