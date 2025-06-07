package by.grgu.identityservice.service;

import by.grgu.identityservice.database.entity.*;
import by.grgu.identityservice.database.entity.enumm.Role;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.usecaseses.mapper.AuthUserMapper;
import by.grgu.identityservice.utils.JwtTokenUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final RestTemplate restTemplate;
    private final String ACCOUNT_SERVICE_URL = "http://localhost:8082/accounts";
    private final String GATEWAY_SERVICE_URL = "http://localhost:8082/gateway/update-token";

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthUserMapper authUserMapper, JwtTokenUtil jwtTokenUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUserMapper = authUserMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.restTemplate = restTemplate;
    }

    public User register(RegistrationRequest request) {
        if (request.getPassword() == null) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь уже существует!");
        }

        Role assignedRole = Role.USER;
        if (request.getRole() != null) {
            try {
                assignedRole = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Недопустимая роль: " + request.getRole());
            }
        }

        User user = authUserMapper.toUser(request, passwordEncoder);
        user.setRole(assignedRole);

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

    public boolean updateUserFields(String oldUsername, Map<String, String> updatedData) {
        Optional<User> userOptional = userRepository.findByUsername(oldUsername);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String newUsername = updatedData.get("username");

            if (!oldUsername.equals(newUsername)) {
                user.setUsername(newUsername);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        user,null,
                        List.of(new SimpleGrantedAuthority(user.getRole().getAuthority())));
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            user.setFirstname(updatedData.get("firstname"));
            user.setLastname(updatedData.get("lastname"));
            user.setEmail(updatedData.get("email"));
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    public void sendToken(String username, String token) {
        sendTokenToApiGateway(username, token);
    }

    public void sendTokenToApiGateway(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.set("username", username);
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
}