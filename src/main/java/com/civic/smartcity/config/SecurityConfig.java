package com.civic.smartcity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.civic.smartcity.security.JwtAuthFilter;
import com.civic.smartcity.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ── Public Assets ──────────────────────
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/dashboard.html",
                    "/submit.html",
                    "/mygrievances.html",
                    "/admin.html",
                    "/admingrievances.html",
                    "/officer.html",
                    "/dashboard",
                    "/submit",
                    "/mygrievances",
                    "/favicon.ico",
                    "/h2-console/**",
                    "/css/**", "/js/**", "/images/**"
                ).permitAll()
                // ── Auth API ───────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                // ── Module 2: Citizen ──────────────────
                .requestMatchers("/api/module2/**").hasRole("CITIZEN")
                // ── Module 3: Admin & Officer ──────────
                .requestMatchers("/api/module3/**").hasAnyRole("ADMIN", "OFFICER")
                // ── Module 4: Officer ──────────────────
                .requestMatchers("/api/module4/**").hasAnyRole("ADMIN", "OFFICER")
                // ── Module 5: Feedback ─────────────────
                .requestMatchers("/api/module5/**").hasRole("CITIZEN")
                // ── Legacy/Other APIs ──────────────────
                .requestMatchers("/api/grievances/**").authenticated()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
