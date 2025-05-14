package by.grgu.reportgenerationservice.database.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "total_reports")
public class TotalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TotalReport(String username, BigDecimal totalIncome, BigDecimal totalExpense) {
        this.username = username;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.createdAt = LocalDateTime.now();
    }
}