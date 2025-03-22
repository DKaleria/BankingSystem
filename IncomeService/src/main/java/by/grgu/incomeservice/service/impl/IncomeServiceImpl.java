package by.grgu.incomeservice.service.impl;

import by.grgu.incomeservice.database.entity.Income;
import by.grgu.incomeservice.database.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class IncomeServiceImpl {

    private final IncomeRepository incomeRepository;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    /**
     * Получить общий доход за указанный месяц и год.
     *
     * @param userId идентификатор пользователя
     * @param month  месяц (1-12)
     * @param year   год
     * @return общий доход
     */
    public BigDecimal getTotalIncomeForMonth(UUID userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Получить список доходов за указанный месяц и год.
     *
     * @param userId идентификатор пользователя
     * @param month  месяц (1-12)
     * @param year   год
     * @return список доходов
     */
    public List<Income> getIncomesForMonth(UUID userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    /**
     * Получить список всех доходов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список доходов
     */
    public List<Income> getAllIncomes(UUID userId) {
        return incomeRepository.findByUserId(userId);
    }

    /**
     * Сохранить новый доход.
     *
     * @param income новый доход
     * @return сохраненный доход
     */
    public Income saveIncome(Income income) {
        return incomeRepository.save(income);
    }
}