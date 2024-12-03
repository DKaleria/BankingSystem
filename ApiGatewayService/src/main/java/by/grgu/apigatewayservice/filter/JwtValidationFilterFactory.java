package by.grgu.apigatewayservice.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
public class JwtValidationFilterFactory implements GatewayFilterFactory<JwtValidationFilterFactory.Config> {
    private final RestTemplate restTemplate;
    private final String identityServiceUrl;

    public JwtValidationFilterFactory(RestTemplate restTemplate,
                                      @Value("${identity.service.url}") String identityServiceUrl) {
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
    }

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println("JWT Validation Filter is called");
        return (exchange, chain) -> {
            String token = extractTokenFromRequest(exchange);
            if (validateToken(token)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean validateToken(String token) {
        System.out.println("validateToken() token = " + token);
        if (token != null) {
            String url = identityServiceUrl + "/identity/validate-token?Authorization=" + token;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        Void.class);
                return response.getStatusCode().is2xxSuccessful();
            } catch (RestClientException e) {
                System.out.println("Error during token validation: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            System.out.println("authorizationHeader.substring(7): "+ authorizationHeader.substring(7));
            return authorizationHeader.substring(7);
        }
        return null;
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of();
    }

    public static class Config {

    }
}