package by.grgu.apigatewayservice.controller;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/gateway")
public class ApiGatewayController {
    private HttpHeaders savedHeaders = new HttpHeaders();

    @PostMapping("/update-token")
    public ResponseEntity<Void> updateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                            @RequestHeader("username") String username) {
        if (authorizationHeader == null || username == null) {
            System.err.println("❌ Ошибка: отсутствуют заголовки!");
            return ResponseEntity.badRequest().build();
        }

        System.out.println("✅ Получен токен: " + authorizationHeader + " для пользователя " + username);

        // ✅ Сохраняем заголовки, чтобы потом их вернуть
        savedHeaders.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        savedHeaders.set("username", username);

        System.out.println("savedHeaders: " + savedHeaders.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-token")
    public ResponseEntity<Map<String, String>> getToken() {
        System.out.println("✅ Получен запрос на /get-token");

        if (savedHeaders.isEmpty()) {
            System.err.println("❌ Ошибка: заголовки не найдены!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ Преобразуем `HttpHeaders` в `Map<String, String>`, чтобы фильтр видел заголовки
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HttpHeaders.AUTHORIZATION, savedHeaders.getFirst(HttpHeaders.AUTHORIZATION));
        headersMap.put("username", savedHeaders.getFirst("username"));

        return ResponseEntity.ok(headersMap);
    }

}


/*
@Controller
@RequestMapping("/gateway")
public class ApiGatewayController {
    private ApiGatewayService apiGatewayService;
    private RestTemplate restTemplate;

    public ApiGatewayController(ApiGatewayService apiGatewayService, RestTemplate restTemplate) {
        this.apiGatewayService = apiGatewayService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/update-token")
    public ResponseEntity<Void> updateToken(@RequestHeader("username") String username,
                                            @RequestHeader("token") String token) {
        if (token == null || username == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            apiGatewayService.saveToken(username, token);
            System.out.println("Token saved: " + token + " for user: " + username);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении токена: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}*/

