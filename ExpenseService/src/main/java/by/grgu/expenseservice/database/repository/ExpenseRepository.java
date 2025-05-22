package by.grgu.expenseservice.database.repository;

import by.grgu.expenseservice.database.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT DISTINCT e.description FROM Expense e WHERE e.username = :username")
    List<String> getExpenseDescriptions(@Param("username") String username);

    List<Expense> findByUsername(String username);

    @Query("SELECT e FROM Expense e WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> getExpensesForMonth(@Param("username") String username,
                                      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Expense> findByUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM Expense e WHERE e.username = :username AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    List<Expense> findByUsernameAndMonth(@Param("username") String username, @Param("month") int month, @Param("year") int year);

    @Query("SELECT e FROM Expense e WHERE e.username = :username AND e.description = :description AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> getExpenseByDescriptionForMonth(@Param("username") String username,
                                                  @Param("description") String description,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

}
