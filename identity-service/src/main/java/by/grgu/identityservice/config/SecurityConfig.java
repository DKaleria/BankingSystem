package by.grgu.identityservice.config;

import by.grgu.identityservice.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    private static final String[] WHITELIST = {
            "/identity/register",
            "/identity/login",
            "/identity/logout",
            "/identity/exit",
            "/identity/registration",
            "/identity/authenticate",
            "/identity/validate-token"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST)
                        .permitAll()
                        .requestMatchers("/identity/users").hasAnyAuthority("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(
                        login -> login
                                .loginPage("/identity/login")
                                .defaultSuccessUrl("/identity/main", true)
                                .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/identity/logout")
                        .logoutSuccessUrl("/identity/login")
                        .permitAll())
                .build();
    }
}
