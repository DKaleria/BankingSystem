package by.grgu.expenseservice.service.impl;

import by.grgu.expenseservice.database.entity.Expense;
import by.grgu.expenseservice.database.repository.ExpenseRepository;
import by.grgu.expenseservice.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, RestTemplate restTemplate) {
        this.expenseRepository = expenseRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Expense createExpense(Expense expense) {
        System.out.println("📌 Запрос на добавление расхода, username: " + expense.getUsername());

        // ✅ Получаем общий доход пользователя
        String totalIncomeUrl = "http://localhost:8082/incomes/" + expense.getUsername() + "/total";
        ResponseEntity<BigDecimal> incomeResponse = restTemplate.getForEntity(totalIncomeUrl, BigDecimal.class);
        BigDecimal totalIncome = (incomeResponse.getBody() != null) ? incomeResponse.getBody() : BigDecimal.ZERO;

        System.out.println("📌 Общий доход пользователя: " + totalIncome);

        // ✅ Получаем общий расход пользователя перед добавлением нового расхода
        String totalExpenseUrl = "http://localhost:8082/expenses/" + expense.getUsername() + "/total";
        ResponseEntity<BigDecimal> expenseResponse = restTemplate.getForEntity(totalExpenseUrl, BigDecimal.class);
        BigDecimal totalExpense = (expenseResponse.getBody() != null) ? expenseResponse.getBody() : BigDecimal.ZERO;

        System.out.println("📌 Общий расход до добавления нового расхода: " + totalExpense);

        // ✅ Проверяем, что расход не приведет к отрицательному балансу
        BigDecimal projectedBalance = totalIncome.subtract(totalExpense.add(BigDecimal.valueOf(expense.getAmount())));

        if (projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("❌ Ошибка: Расход превышает допустимый лимит!");
            throw new IllegalArgumentException("❌ Ошибка: Ваш расход превышает доступный баланс!");
        }

        // ✅ Сохраняем расход, если проверка пройдена
        return expenseRepository.save(expense);
    }


    @Override
    public List<Expense> getAllExpenses(String username) {
        return expenseRepository.findByUsername(username);
    }

    @Override
    public BigDecimal getTotalExpenseForMonth(String username, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Expense> expenses = expenseRepository.findByUsernameAndDateBetween(username, startDate, endDate);
        return expenses.stream()
                .map(expense -> BigDecimal.valueOf(expense.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Expense> getExpensesForMonth(String username, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        System.out.println("📌 Запрос расходов с " + startDate + " по " + endDate);

        return expenseRepository.getExpensesForMonth(username, startDate, endDate);
    }

    public Map<String, BigDecimal> getExpenseBreakdown(String username, int month, int year) {
        List<Expense> expenses = expenseRepository.findByUsernameAndMonth(username, month, year);

        return expenses.stream()
                .collect(Collectors.groupingBy(Expense::getSource,
                        Collectors.reducing(BigDecimal.ZERO, expense -> BigDecimal.valueOf(expense.getAmount()), BigDecimal::add)));
    }
}
