package by.grgu.reportgenerationservice.dto;

import lombok.*;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDTO {
    private String username;
    private BigDecimal amount;
}