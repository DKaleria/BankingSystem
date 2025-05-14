package by.grgu.expenseservice.controller;

import by.grgu.expenseservice.database.entity.Expense;
import by.grgu.expenseservice.dto.ExpenseDTO;
import by.grgu.expenseservice.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseApiController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/monthly")
    public ResponseEntity<List<ExpenseDTO>> getMonthlyExpensesApi(@RequestHeader("username") String username,
                                                                  @RequestParam int month,
                                                                  @RequestParam int year) {
        System.out.println("📌 API-запрос на JSON-расходы за месяц, username: " + username);

        List<Expense> expenses = expenseService.getExpensesForMonth(username, month, year);

        // ✅ Теперь `description` точно передается!
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(expense -> new ExpenseDTO(expense.getUsername(),
                        BigDecimal.valueOf(expense.getAmount()),
                        expense.getDescription()))  // ✅ Описание добавлено!
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(expenseDTOs);
    }
}


