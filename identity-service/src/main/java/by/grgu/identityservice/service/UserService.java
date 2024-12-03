package by.grgu.identityservice.service;

//import com.example.demo.events.AuthUserGotEvent;

import by.grgu.identityservice.database.entity.CustomUserDetails;
import by.grgu.identityservice.database.entity.RegistrationRequest;
import by.grgu.identityservice.database.entity.User;
import by.grgu.identityservice.database.entity.enumm.Role;
import by.grgu.identityservice.database.repository.UserRepository;
import by.grgu.identityservice.usecaseses.mapper.AuthUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;
//    private KafkaTemplate<String, AuthUserGotEvent> kafkaTemplate;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthUserMapper authUserMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUserMapper = authUserMapper;
    }

    public User register(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        User user = User.builder()
                .username(request.getUsername())
                .birthDate(request.getBirthDate())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return userRepository.findByUsername(username)
//                .map(CustomUserDetails::new)
//                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
//    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Извлекаем роль и создаем List<GrantedAuthority>
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
                    // Создаем CustomUserDetails, передавая только user
                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    // Устанавливаем роли
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