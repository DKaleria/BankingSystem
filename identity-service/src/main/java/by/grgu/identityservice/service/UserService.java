package by.grgu.identityservice.service;

//import com.example.demo.events.AuthUserGotEvent;
import by.grgu.identityservice.database.entity.AccountRequest;
import by.grgu.identityservice.database.entity.CustomUserDetails;
import by.grgu.identityservice.database.entity.RegistrationRequest;
import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.database.entity.enumm.Role;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.usecaseses.mapper.AuthUserMapper;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;
    private final RestTemplate restTemplate;
    //    private KafkaTemplate<String, AuthUserGotEvent> kafkaTemplate;
    private final String ACCOUNT_SERVICE_URL = "http://account-service/accounts";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthUserMapper authUserMapper, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUserMapper = authUserMapper;
        this.restTemplate = restTemplate;
    }

    public User register(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = authUserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
            throw new IllegalArgumentException("Account already exists for user: " + accountRequest.getUsername());
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
}