package by.grgu.identityservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(
        Duration accessTokenValidity,
        Duration refreshTokenValidity,
        String secret
) {}