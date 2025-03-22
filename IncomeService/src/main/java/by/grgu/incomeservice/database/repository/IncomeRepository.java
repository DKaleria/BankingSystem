package by.grgu.incomeservice.database.repository;

import by.grgu.incomeservice.database.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncomeRepository extends JpaRepository<Income, UUID> {

    List<Income> findByUserId(UUID userId);

    List<Income> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

}