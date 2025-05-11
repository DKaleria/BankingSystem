package by.grgu.apigatewayservice.filter;

import by.grgu.apigatewayservice.config.IdentityServiceConfig;
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
            System.out.println("✅ Все заголовки запроса в фильтре: " + exchange.getRequest().getHeaders());

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            // ✅ Если заголовки отсутствуют, получаем их сразу из `update-token`
            if (authHeader == null || username == null) {
                Map<String, String> fetchedHeaders = fetchHeadersFromApiGateway();
                System.out.println("✅ Заголовки из API Gateway: " + fetchedHeaders);

                if (fetchedHeaders != null) {
                    authHeader = fetchedHeaders.get(HttpHeaders.AUTHORIZATION);
                    username = fetchedHeaders.get("username");
                } else {
                    System.err.println("❌ Ошибка: API Gateway не вернул заголовки!");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            String token = authHeader.substring(7);

            // ✅ Проверяем токен через Identity Service
            if (!validateToken(token)) {
                System.err.println("❌ Ошибка: токен не прошел проверку!");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // ✅ Передаем заголовки в другие сервисы
            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("username", username)
                    .build();

            return chain.filter(exchange);
        };
    }

    private Map<String, String> fetchHeadersFromApiGateway() {
        String url = "http://localhost:8082/gateway/get-token"; // ✅ Теперь делаем `GET`, а не `POST`

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            System.out.println("✅ Ответ от API Gateway: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // ✅ Теперь заголовки приходят в `Map<String, String>`
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении заголовков из API Gateway: " + e.getMessage());
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
            System.err.println("❌ Ошибка при валидации токена: " + e.getMessage());
            return false;
        }
    }
    public static class Config {
    }

}


/*
@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final IdentityServiceConfig identityServiceConfig;
    private final RestTemplate restTemplate;

    public JwtValidationFilter(IdentityServiceConfig identityServiceConfig, RestTemplate restTemplate) {
        super(Config.class);
        this.identityServiceConfig = identityServiceConfig;
        this.restTemplate = restTemplate;
    }

    /*@Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Все заголовки запроса: " + exchange.getRequest().getHeaders());

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            if (authHeader == null || username == null) {
                HttpHeaders fetchedHeaders = fetchHeadersFromApiGateway();
                if (fetchedHeaders != null) {
                    authHeader = fetchedHeaders.getFirst(HttpHeaders.AUTHORIZATION);
                    username = fetchedHeaders.getFirst("username");
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
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Все заголовки запроса в фильтре: " + exchange.getRequest().getHeaders());

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            // Если заголовки отсутствуют, пробуем получить их из API Gateway
            if (authHeader == null || username == null) {
                HttpHeaders fetchedHeaders = fetchHeadersFromApiGateway();
                System.out.println("Заголовки из API Gateway: " + fetchedHeaders);
                if (fetchedHeaders != null) {
                    authHeader = fetchedHeaders.getFirst(HttpHeaders.AUTHORIZATION);
                    username = fetchedHeaders.getFirst("username");
                } else {
                    System.err.println("Ошибка: API Gateway не вернул заголовки!");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            String token = authHeader.substring(7);

            // Проверяем токен через Identity Service
            if (!validateToken(token)) {
                System.err.println("Ошибка: токен не прошел проверку!");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Передаем заголовки в другие сервисы
            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("username", username)
                    .build();

            return chain.filter(exchange);
        };
    }


    public static class Config {
    }

    /*private HttpHeaders fetchHeadersFromApiGateway() {
        String url = "http://localhost:8082/gateway/update-token";

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getHeaders();
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении заголовков из API Gateway: " + e.getMessage());
        }
        return null;
    }*/
/*
    private HttpHeaders fetchHeadersFromApiGateway() {
        String url = "http://localhost:8082/gateway/update-token"; // ✅ Запрос на `POST`

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/json"); // ✅ Указываем, что ждем JSON-ответ

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
            System.out.println("Ответ от API Gateway: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Заголовки из API Gateway: " + response.getHeaders());
                return response.getHeaders(); // ✅ Заголовки приходят корректно
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении заголовков из API Gateway: " + e.getMessage());
        }
        return null;
    }




    private boolean validateToken(String token) {
        String url = identityServiceConfig.getServiceUrl() + "/identity/validate-token";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.out.println("Ошибка при валидации токена: " + e.getMessage());
            return false;
        }
    }
}

*/
/*
@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final IdentityServiceConfig identityServiceConfig;
    private final RestTemplate restTemplate;

    public JwtValidationFilter(IdentityServiceConfig identityServiceConfig, RestTemplate restTemplate) {
        super(Config.class);
        this.identityServiceConfig = identityServiceConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Все заголовки запроса: " + exchange.getRequest().getHeaders());

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete(); // ❌ Если токена нет в заголовках — сразу отказ
            }

            String token = authHeader.substring(7);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            // Если username отсутствует, но есть токен, пробуем найти его в базе
            if (username == null) {
                username = getUsernameByToken(token);
                if (username == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete(); // ❌ Если не найден username — отказ
                }
            }

            // Проверяем токен через Identity Service
            if (!validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete(); // ❌ Если токен невалидный — отказ
            }

            // Если все проверки пройдены, добавляем заголовки и передаем дальше
            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("username", username)
                    .build();
            return chain.filter(exchange);
        };
    }

    private boolean validateToken(String token) {
        String url = identityServiceConfig.getServiceUrl() + "/identity/validate-token?Authorization=" + token;

        try {
            System.out.println("Валидация токена: " + token);
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

    // Получение username по токену (но НЕ токена по username!)
    private String getUsernameByToken(String token) {
        return userTokenRepository.findByToken(token)
                .map(UserToken::getUsername)
                .orElse(null);
    }

    @Override
    public String name() {
        return "JwtValidationFilter";
    }

    public static class Config {
    }
}
*/


/*@Component
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
            System.out.println("Все заголовки запроса: " + exchange.getRequest().getHeaders());

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String username = exchange.getRequest().getHeaders().getFirst("username");

            if (authHeader != null && authHeader.startsWith("Bearer ") && username != null) {
                String token = authHeader.substring(7);

                // Валидация токена на Identity сервисе
                if (validateToken(token)) {
                    exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .header("username", username)
                            .build();
                    return chain.filter(exchange);
                }
            }

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        };
    }

    private boolean validateToken(String token) {
        String url = identityServiceConfig.getServiceUrl() + "/identity/validate-token?Authorization=" + token;

        try {
            System.out.println("Валидация токена: " + token);
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
*/

/*@Component
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
                        // Установка токена в заголовок запроса для других сервисов
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
}*/

/*
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
*/