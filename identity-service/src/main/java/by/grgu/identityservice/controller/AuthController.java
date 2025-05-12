package by.grgu.identityservice.controller;

import by.grgu.identityservice.database.entity.AuthenticationRequest;
import by.grgu.identityservice.database.entity.AuthenticationResponse;
import by.grgu.identityservice.database.entity.RegistrationRequest;
import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.exceptions.ErrorMessage;
import by.grgu.identityservice.service.UserService;
import by.grgu.identityservice.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Date;
import java.util.List;

@Controller
@AllArgsConstructor
@Transactional
@RequestMapping("/identity")
public class AuthController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @GetMapping("/main")
    public String showMainPage() {
        return "main";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        System.out.println("Страница регистрации вызывается");
        System.out.println("Registration page called");
        model.addAttribute("createUserRequest", new RegistrationRequest());
        return "registration";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegistrationRequest request,
                                           HttpServletResponse response) {
        System.out.println("Register method called");
        System.out.println("Password in controller method: " + request.getPassword());

        userService.register(request);

        return "registration_success";
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @SneakyThrows
    @PostMapping("/authenticate")
    public String authenticate(@RequestParam String username, @RequestParam String password, RedirectAttributes redirectAttributes) {
        System.out.println("Authenticate method called with username: " + username);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (authentication.isAuthenticated()) {
                String accessToken = jwtTokenUtil.generateAccessToken(authentication);
                String refreshToken = jwtTokenUtil.generateRefreshToken(authentication);

                System.out.println("Отправка токена в UserService");
                userService.sendToken(username, accessToken);

                // Перенаправление на страницу аккаунта
                return "redirect:http://localhost:8082/accounts/account";
            }
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "Неверное имя пользователя или пароль.");
        }

        // Перенаправление на страницу логина с ошибкой
        return "redirect:/identity/login";
    }
/*
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestParam String username, @RequestParam String password) {
        System.out.println("Authenticate method called with username: " + username);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (authentication.isAuthenticated()) {
            String accessToken = jwtTokenUtil.generateAccessToken(authentication);
            String refreshToken = jwtTokenUtil.generateRefreshToken(authentication);

            System.out.println("Отправка токена в UserService");
            userService.sendToken(username, accessToken);

            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            throw new RuntimeException("invalid access");
        }
    }
    */

   /* @SneakyThrows
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        System.out.println("Authenticate method called with username: " + request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (authentication.isAuthenticated()) {
            String accessToken = jwtTokenUtil.generateAccessToken(authentication);
            String refreshToken = jwtTokenUtil.generateRefreshToken(authentication);


            System.out.println("Отправка токена в UserService");
            userService.sendToken(request.getUsername(), accessToken);

            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            throw new RuntimeException("invalid access");
        }
    }*/



    /*@SneakyThrows
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authentication.isAuthenticated()) {
            String accessToken = jwtTokenUtil.generateAccessToken(authentication);
            String refreshToken = jwtTokenUtil.generateRefreshToken(authentication);
            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            throw new RuntimeException("invalid access");
        }
    }*/


    /*@SneakyThrows
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authentication.isAuthenticated()) {
            String jwt = jwtTokenUtil.generateToken(authentication);
            System.out.println("Generated token: " + jwt);
            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        }else {
            throw new RuntimeException("invalid access");
        }
    }*/

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser() {
        try {
            User currentUser = userService.getCurrentUser();
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(new Date(), e.getMessage()));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (jwtTokenUtil.validateToken(authorization.substring(7))) {  // Убираем "Bearer "
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    /*@GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestParam("Authorization") String authorization) {
        if (jwtTokenUtil.validateToken(authorization)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }*/

    @GetMapping("/exit")
    public String showExitPage() {
        return "logout";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Метод выхода вызывается");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "login";
    }

}