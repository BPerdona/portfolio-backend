package com.portfolio.portfoliobackend.security.configs;

import com.portfolio.portfoliobackend.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((manager) ->{
                manager.requestMatchers("/api/v1/auth/**").permitAll().anyRequest().authenticated();
            })
            .sessionManagement((session) -> {
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout()
            .logoutUrl("/api/v1/auth/logout")
            .addLogoutHandler(logoutHandler)
            .logoutSuccessHandler((request, response, authentication)-> SecurityContextHolder.clearContext());


        return http.build();
    }
}
