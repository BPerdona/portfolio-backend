package com.portfolio.portfoliobackend.auth;

import com.portfolio.portfoliobackend.repositories.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        jwtToken = authHeader.substring(7);
        if (jwtToken.isBlank()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var storedToken = tokenRepository.findByToken(jwtToken).orElse(null);
        if(storedToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        storedToken.setExpired(true);
        storedToken.setRevoked(true);

        tokenRepository.save(storedToken);
    }
}
