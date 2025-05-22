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
            return ResponseEntity.badRequest().build();
        }

        savedHeaders.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        savedHeaders.set("username", username);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-token")
    public ResponseEntity<Map<String, String>> getToken() {
        if (savedHeaders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HttpHeaders.AUTHORIZATION, savedHeaders.getFirst(HttpHeaders.AUTHORIZATION));
        headersMap.put("username", savedHeaders.getFirst("username"));

        return ResponseEntity.ok(headersMap);
    }
}