package by.grgu.incomeservice.controller;

import by.grgu.incomeservice.database.entity.Income;
import by.grgu.incomeservice.dto.IncomeDTO;
import by.grgu.incomeservice.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeApiController {

    @Autowired
    private IncomeService incomeService;

    @GetMapping("/monthly")
    public ResponseEntity<List<IncomeDTO>> getMonthlyIncomesApi(@RequestHeader("username") String username,
                                                                @RequestParam int month,
                                                                @RequestParam int year) {
        System.out.println("📌 API-запрос на JSON-доходы за месяц, username: " + username);

        List<Income> incomes = incomeService.getIncomesForMonth(username, month, year);

        // ✅ Преобразуем `Income` в `IncomeDTO`
        List<IncomeDTO> incomeDTOs = incomes.stream()
                .map(income -> new IncomeDTO(income.getUsername(),
                        BigDecimal.valueOf(income.getAmount()),
                        income.getSource()))  // ✅ Добавляем источник дохода!
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(incomeDTOs);
    }
}
