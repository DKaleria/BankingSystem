package by.grgu.incomeservice.database.repository;

import by.grgu.incomeservice.database.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUsername(String username);

    @Query("SELECT i FROM Income i WHERE i.username = :username AND i.date BETWEEN :startDate AND :endDate")
    List<Income> getIncomesForMonth(@Param("username") String username, @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    List<Income> findByUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT i.source FROM Income i WHERE i.username = :username")
    List<String> getDistinctIncomeSources(@Param("username") String username);

    @Query("SELECT i FROM Income i WHERE i.username = :username " +
            "AND i.source = :source AND i.date BETWEEN :startDate AND :endDate")
    List<Income> getIncomeBySourceForMonth(@Param("username") String username,
                                           @Param("source") String source,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
