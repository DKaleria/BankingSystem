package by.grgu.incomeservice.service;

import by.grgu.incomeservice.database.entity.Income;

import java.util.List;

public interface IncomeService{
    Income createIncome(Income income);
    List<Income> getAllIncomes();

}
