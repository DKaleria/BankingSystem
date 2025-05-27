package by.grgu.identityservice.controller;

import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.service.UserService;
import by.grgu.identityservice.utils.JwtTokenUtil;
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
                "/user-accounts/updateField",
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

        boolean identityUpdateSuccess = userService.updateUserFields(oldUsername, updatedData);
        String newUsername = updatedData.get("username");

        if (identityUpdateSuccess && !newUsername.equals(oldUsername)) {
            UserDetails userDetails = userService.loadUserByUsername(newUsername);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            User user = userRepository.findByUsername(newUsername)
                    .orElseThrow(() -> new RuntimeException("Ошибка: Не удалось найти пользователя для сохранения!"));

            userRepository.save(user);

            String newToken = jwtTokenUtil.generateAccessToken(newAuth);

            userService.sendToken(newUsername, newToken);
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Данные обновлены, SecurityContext и API Gateway обновлены!"
        ));
    }
}
