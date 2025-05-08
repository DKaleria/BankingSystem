package by.grgu.apigatewayservice.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tokens")
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
