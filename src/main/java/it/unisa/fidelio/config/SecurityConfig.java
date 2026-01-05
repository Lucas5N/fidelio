package it.unisa.fidelio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabilitiamo quello che non serve per un'app REST
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // 1. Risorse statiche (CSS, JS, Immagini) sempre accessibili
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 2. Endpoint API di autenticazione e registrazione pubblici
                        .requestMatchers("/api/auth/**", "/api/registrazione/**").permitAll()

                        // 3. Rotte del frontend (Thymeleaf) pubbliche
                        .requestMatchers("/", "/home", "/login", "/signup", "/movies", "/lists", "/profile", "/diary").permitAll()

                        // 4. Per ora permettiamo tutto il resto (anyRequest) per non bloccare i test
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}