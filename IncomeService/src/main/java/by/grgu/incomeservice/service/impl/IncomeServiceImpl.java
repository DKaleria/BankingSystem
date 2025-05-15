package by.grgu.incomeservice.service.impl;

import by.grgu.incomeservice.database.entity.Income;
import by.grgu.incomeservice.database.repository.IncomeRepository;
import by.grgu.incomeservice.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final RestTemplate restTemplate; // ✅ Добавляем RestTemplate

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, RestTemplate restTemplate) {
        this.incomeRepository = incomeRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Income createIncome(Income income) {
        String birthDateUrl = "http://localhost:8082/accounts/" + income.getUsername() + "/birthdate";
        ResponseEntity<LocalDate> response = restTemplate.getForEntity(birthDateUrl, LocalDate.class);

        if (response.getStatusCode().isError() || response.getBody() == null) {
            throw new IllegalArgumentException("❌ Ошибка: Не удалось получить дату рождения!");
        }

        LocalDate birthDate = response.getBody();

        if (income.getDate().isBefore(birthDate)) {
            throw new IllegalArgumentException("Ошибка: доход не может быть записан до рождения!");
        }

        return incomeRepository.save(income);
    }


    @Override
    public List<Income> getAllIncomes(String username) {
        return incomeRepository.findByUsername(username);
    }

    @Override
    public BigDecimal getTotalIncomeForMonth(String username, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Income> incomes = incomeRepository.findByUsernameAndDateBetween(username, startDate, endDate);
        return incomes.stream()
                .map(income -> BigDecimal.valueOf(income.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Income> getIncomesForMonth(String username, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return incomeRepository.getIncomesForMonth(username, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalIncomeForUser(String username) {
        List<Income> incomes = incomeRepository.findByUsername(username);

        return incomes.stream()
                .map(income -> BigDecimal.valueOf(income.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
