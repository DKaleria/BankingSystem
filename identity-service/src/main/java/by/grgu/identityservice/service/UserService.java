package by.grgu.identityservice.service;

//import com.example.demo.events.AuthUserGotEvent;

import by.grgu.identityservice.database.entity.*;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.usecaseses.mapper.AuthUserMapper;
import by.grgu.identityservice.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final RestTemplate restTemplate;
    //    private KafkaTemplate<String, AuthUserGotEvent> kafkaTemplate;
    // private final String ACCOUNT_SERVICE_URL = "http://account-service/accounts";
    private final String ACCOUNT_SERVICE_URL = "http://localhost:8099/accounts";
    private final String GATEWAY_SERVICE_URL = "http://localhost:8082/gateway/update-token";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthUserMapper authUserMapper, JwtTokenUtil jwtTokenUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUserMapper = authUserMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.restTemplate = restTemplate;
    }

    public User register(RegistrationRequest request) {
        System.out.println("Registration Request: " + request);

        if (request.getPassword() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = authUserMapper.toUser(request, passwordEncoder);
        System.out.println("User after mapping: " + user);
        createAccountForUser(user);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private void createAccountForUser(User user) {
        AccountRequest accountRequest = AccountRequest.builder()
                .username(user.getUsername())
                .birthDate(user.getBirthDate())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .registrationDate(LocalDate.now())
                .active(true)
                .role(user.getRole())
                .password(user.getPassword())
                .build();
        System.out.println("createAccountForUser: " + accountRequest);
        ResponseEntity<Void> response = restTemplate.exchange(
                ACCOUNT_SERVICE_URL,
                HttpMethod.POST,
                new HttpEntity<>(accountRequest, new HttpHeaders()),
                Void.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Account successfully created for user: " + accountRequest.getUsername());
        } else if (response.getStatusCode().value() == 409) {
            throw new IllegalArgumentException("Account already exists for user: " + accountRequest.toString());
        } else {
            throw new RuntimeException("Failed to create account for user: " + accountRequest.getUsername());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    customUserDetails.setAuthorities(authorities);
                    return customUserDetails;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
    }

    public User getCurrentUser() throws ExecutionException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user;
        }
        throw new RuntimeException("Authentication is not valid");
    }

    public void sendToken(String username, String token) {
        System.out.println("Отправка токена: " + token + " для пользователя: " + username);

        sendTokenToApiGateway(username, token);
    }

    private void sendTokenToApiGateway(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", username);
        headers.set("token", token);

        System.out.println("Заголовки перед отправкой в API Gateway: " + headers);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(GATEWAY_SERVICE_URL, requestEntity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Токен успешно отправлен в API Gateway");
            } else {
                System.out.println("Ошибка при отправке токена в API Gateway: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отправке токена в API Gateway: " + e.getMessage());
        }
    }

    /*private void sendTokenToApiGateway(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", username);
        headers.set("token", token);

        System.out.println("Заголовки перед отправкой в API Gateway: " + headers);

        restTemplate.postForEntity(GATEWAY_SERVICE_URL, null, Void.class, headers);
    }*/

    /*private void sendTokenToApiGateway(String username, String token) {
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("token", token);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request);
        System.out.println("request in UserService send token: "+ request);
        restTemplate.postForEntity(GATEWAY_SERVICE_URL, entity, Void.class);
    }*/
    }