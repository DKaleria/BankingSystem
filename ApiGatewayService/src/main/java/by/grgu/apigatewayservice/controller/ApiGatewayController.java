package by.grgu.apigatewayservice.controller;

import by.grgu.apigatewayservice.service.ApiGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gateway")
public class ApiGatewayController {
    private ApiGatewayService apiGatewayService;

    public ApiGatewayController(ApiGatewayService apiGatewayService) {
        this.apiGatewayService = apiGatewayService;
    }

    @PostMapping("/update-token")
    public ResponseEntity<Void> updateToken(@RequestHeader("username") String username,
                                            @RequestHeader("token") String token) {
        if (token == null || username == null) {
            return ResponseEntity.badRequest().build(); // Возвращаем 400, если заголовки отсутствуют
        }

        try {
            apiGatewayService.saveToken(username, token);
            System.out.println("Token saved: " + token + " for user: " + username);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении токена: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Возвращаем 500 в случае ошибки
        }
    }
}