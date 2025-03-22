package by.grgu.incomeservice.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeRequest {
    private UUID userId;
    private BigDecimal amount;
    private String source;
    private LocalDate date;
    private String description;
}