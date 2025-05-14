package by.grgu.reportgenerationservice.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlyReportDTO {
    private String username;
    private int month;
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private List<IncomeDTO> incomes;
    private List<ExpenseDTO> expenses;

    public MonthlyReportDTO(String username, int month, int year, BigDecimal totalIncome, BigDecimal totalExpense,
                            List<IncomeDTO> incomes, List<ExpenseDTO> expenses) {
        this.username = username;
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.incomes = incomes;
        this.expenses = expenses;
    }

}