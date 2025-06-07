package by.grgu.apigatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final RestTemplate restTemplate;

    public JwtValidationFilter(RestTemplate restTemplate) {
        super(Config.class);
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            if (authHeader == null || username == null) {
                Map<String, String> fetchedHeaders = fetchHeadersFromApiGateway();

                if (fetchedHeaders != null) {
                    authHeader = fetchedHeaders.get(HttpHeaders.AUTHORIZATION);
                    username = fetchedHeaders.get("username");
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            String token = authHeader.substring(7);

            if (!validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("username", username)
                    .build();

            return chain.filter(exchange);
        };
    }

    private Map<String, String> fetchHeadersFromApiGateway() {
        String url = "http://localhost:8082/gateway/get-token";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении заголовков из API Gateway: " + e.getMessage());
        }
        return null;
    }

    private boolean validateToken(String token) {
        String url = "http://localhost:8088/identity/validate-token";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Ошибка при валидации токена: " + e.getMessage());
            return false;
        }
    }
    public static class Config {
    }
}