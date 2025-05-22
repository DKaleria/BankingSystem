package by.grgu.recommendationservice.database.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class RecommendationReport {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal desiredExpenses;
    private BigDecimal remainingBudget;
    private List<String> recommendations;

    public RecommendationReport(BigDecimal totalIncome,
                                BigDecimal totalExpense, BigDecimal desiredExpenses,
                                BigDecimal remainingBudget, List<String> recommendations) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.desiredExpenses = desiredExpenses;
        this.remainingBudget = remainingBudget;
        this.recommendations = recommendations;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public void setDesiredExpenses(BigDecimal desiredExpenses) {
        this.desiredExpenses = desiredExpenses;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getDesiredExpenses() { return desiredExpenses; }
    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public List<String> getRecommendations() { return recommendations; }
}
