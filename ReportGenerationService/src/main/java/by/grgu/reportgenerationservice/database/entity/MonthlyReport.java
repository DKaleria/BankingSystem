package by.grgu.reportgenerationservice.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "monthly_reports")
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int month;
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MonthlyReport(String username, int month, int year, BigDecimal totalIncome, BigDecimal totalExpense) {
        this.username = username;
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.createdAt = LocalDateTime.now();
    }
}