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

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RestTemplate restTemplate; // ✅ Добавляем RestTemplate

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, RestTemplate restTemplate) {
        this.expenseRepository = expenseRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Expense createExpense(Expense expense) {
        // ✅ Получаем дату рождения пользователя из `AccountService`
        String birthDateUrl = "http://localhost:8082/accounts/" + expense.getUsername() + "/birthdate";
        ResponseEntity<LocalDate> birthDateResponse = restTemplate.getForEntity(birthDateUrl, LocalDate.class);

        if (birthDateResponse.getStatusCode().isError() || birthDateResponse.getBody() == null) {
            throw new IllegalArgumentException("❌ Ошибка: Не удалось получить дату рождения!");
        }

        LocalDate birthDate = birthDateResponse.getBody();

        // ✅ Проверяем, что расход записывается после даты рождения
        if (expense.getDate().isBefore(birthDate)) {
            throw new IllegalArgumentException("❌ Ошибка: Расход не может быть записан до рождения!");
        }

        // ✅ Получаем общий доход пользователя перед сохранением расхода
        String totalIncomeUrl = "http://localhost:8082/incomes/" + expense.getUsername() + "/total";
        ResponseEntity<BigDecimal> incomeResponse = restTemplate.getForEntity(totalIncomeUrl, BigDecimal.class);

        if (incomeResponse.getStatusCode().isError() || incomeResponse.getBody() == null) {
            throw new IllegalArgumentException("❌ Ошибка: Не удалось получить общую сумму доходов!");
        }

        BigDecimal totalIncome = incomeResponse.getBody();

        // ✅ Проверяем, что расход не превышает общий доход
        if (BigDecimal.valueOf(expense.getAmount()).compareTo(totalIncome) > 0) {
            throw new IllegalArgumentException("❌ Ошибка: Расход не может превышать общий доход!");
        }

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
}
