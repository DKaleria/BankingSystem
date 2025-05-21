package by.grgu.accountservice.database.repository;

import by.grgu.accountservice.database.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);

}
