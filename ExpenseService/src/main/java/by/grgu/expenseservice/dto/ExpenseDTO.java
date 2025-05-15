package by.grgu.expenseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private String username;
    private BigDecimal amount;
    private String description;  // ✅ Добавляем поле `description`!
}
