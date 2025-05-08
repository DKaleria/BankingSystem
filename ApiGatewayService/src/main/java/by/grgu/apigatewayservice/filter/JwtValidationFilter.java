package by.grgu.apigatewayservice.filter;

import by.grgu.apigatewayservice.config.IdentityServiceConfig;
import by.grgu.apigatewayservice.database.entity.UserToken;
import by.grgu.apigatewayservice.database.repository.UserTokenRepository;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final UserTokenRepository userTokenRepository;
    private final IdentityServiceConfig identityServiceConfig;
    private final RestTemplate restTemplate;

    public JwtValidationFilter(UserTokenRepository userTokenRepository,
                               IdentityServiceConfig identityServiceConfig,
                               RestTemplate restTemplate) {
        super(Config.class);
        this.userTokenRepository = userTokenRepository;
        this.identityServiceConfig = identityServiceConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Все заголовки: " + exchange.getRequest().getHeaders());

            String username = exchange.getRequest().getHeaders().getFirst("username");
            System.out.println("Username из заголовка в apply() фильтре: " + username);

            if (username != null) {
                Optional<UserToken> userTokenOpt = userTokenRepository.findByUsername(username);
                if (userTokenOpt.isPresent()) {
                    String token = userTokenOpt.get().getToken();

                    // Валидация токена на идентификационном сервисе
                    if (validateToken(token)) {
                        exchange.getRequest().mutate()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build();
                        return chain.filter(exchange);
                    }
                }
            }

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }

    private boolean validateToken(String token) {
        String url = identityServiceConfig.getServiceUrl() + "/identity/validate-token?Authorization=" + token;

        try {
            System.out.println("Валидация токена в фильтре: " + token);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.out.println("Ошибка при валидации токена: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String name() {
        return "JwtValidationFilter";
    }

    public static class Config {
        // Параметры конфигурации, если они понадобятся
    }
}