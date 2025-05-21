package by.grgu.identityservice.controller;

import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.service.UserService;
import by.grgu.identityservice.utils.JwtTokenUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@RestController
@Transactional
@RequestMapping("/user-identity")
public class IdentityController {

    private final JwtTokenUtil jwtTokenUtil;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public IdentityController(RestTemplate restTemplate, UserService userService, UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/updateField")
    public ResponseEntity<Map<String, String>> updateAccountField(
            @RequestHeader("username") String oldUsername,
            @RequestBody Map<String, String> updatedData,
            @RequestHeader("Authorization") String token) {

        updatedData.put("oldUsername", oldUsername);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(updatedData, headers);
        ResponseEntity<Map> accountResponse = restTemplate.exchange(
                "http://localhost:8082/user-accounts/updateField",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (!accountResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", "Ошибка обновления данных в AccountService!"
            ));
        }

        boolean identityUpdateSuccess = userService.updateUserFields(oldUsername, updatedData, token);
        String newUsername = updatedData.get("username");

        if (identityUpdateSuccess && !newUsername.equals(oldUsername)) {
            System.out.println("🔄 Username изменился! Обновляем SecurityContext и API Gateway.");

            UserDetails userDetails = userService.loadUserByUsername(newUsername); // ✅ Загружаем `UserDetails`
            Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            User user = userRepository.findByUsername(newUsername)
                    .orElseThrow(() -> new RuntimeException("❌ Ошибка: Не удалось найти пользователя для сохранения!"));

            userRepository.save(user);

            System.out.println("✅ После сохранения в БД: username = " + user.getUsername());

            // ✅ Генерируем новый JWT-токен с `Authentication`
            String newToken = jwtTokenUtil.generateAccessToken(newAuth);

            // ✅ Отправляем обновленный username и токен в API Gateway
            userService.sendToken(newUsername, newToken);
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Данные обновлены, SecurityContext и API Gateway обновлены!"
        ));
    }

}
