package by.grgu.reportgenerationservice.database.repository;

import by.grgu.reportgenerationservice.database.entity.TotalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TotalReportRepository extends JpaRepository<TotalReport, Long> {
    TotalReport findByUsername(String username);
}