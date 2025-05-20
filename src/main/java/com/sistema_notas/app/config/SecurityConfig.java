package com.sistema_notas.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.sistema_notas.app.repositorio.UserRepository;
import com.sistema_notas.app.entidad.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/login", "/api/users", "/view-credentials", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/coordinator/**").hasRole("COORDINATOR")
                    .requestMatchers("/teacher/**").hasRole("TEACHER")
                    .requestMatchers("/secretary/**").hasRole("SECRETARY")
                    .requestMatchers("/api/secretary/**").hasRole("SECRETARY") 
                    .requestMatchers("/student/**").hasRole("STUDENT")
                    .requestMatchers("/api/student/**").hasRole("STUDENT") 
                    .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    switch (role) {
                        case "ROLE_ADMIN":
                            response.sendRedirect("/admin");
                            break;
                        case "ROLE_COORDINATOR":
                            response.sendRedirect("/coordinator");
                            break;
                        case "ROLE_TEACHER":
                            response.sendRedirect("/teacher");
                            break;
                        case "ROLE_SECRETARY":
                            response.sendRedirect("/secretary");
                            break;
                        case "ROLE_STUDENT":
                            response.sendRedirect("/student");
                            break;
                        default:
                            response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/secretary/**") // Deshabilitar CSRF temporalmente para debugging
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userId -> {
            User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUserId());
            builder.password(user.getPassword());
            builder.roles(user.getRole());
            return builder.build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}