package by.grgu.apigatewayservice.database.repository;

import by.grgu.apigatewayservice.database.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUsername(String username);
    void deleteByUsername(String username);
}