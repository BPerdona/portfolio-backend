package com.portfolio.portfoliobackend.security.configs;

import com.portfolio.portfoliobackend.enums.Role;
import com.portfolio.portfoliobackend.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.portfolio.portfoliobackend.enums.Permission.*;
import static com.portfolio.portfoliobackend.enums.Role.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

                manager.requestMatchers("/api/v1/auth/**").permitAll();

                manager.requestMatchers("/api/v1/playground/**").hasAnyRole(ADMIN.name(), USER.name());
                manager.requestMatchers(HttpMethod.GET, "/api/v1/playground/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name());
                manager.requestMatchers(HttpMethod.POST, "/api/v1/playground/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name());
                manager.requestMatchers(HttpMethod.PUT, "/api/v1/playground/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name());
                manager.requestMatchers(HttpMethod.DELETE, "/api/v1/playground/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name());

                manager.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
                manager.requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasAuthority(ADMIN_READ.name());
                manager.requestMatchers(HttpMethod.POST, "/api/v1/admin/**").hasAuthority(ADMIN_CREATE.name());
                manager.requestMatchers(HttpMethod.PUT, "/api/v1/admin/**").hasAuthority(ADMIN_UPDATE.name());
                manager.requestMatchers(HttpMethod.DELETE, "/api/v1/admin/**").hasAuthority(ADMIN_DELETE.name());

                manager.anyRequest().authenticated();
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
