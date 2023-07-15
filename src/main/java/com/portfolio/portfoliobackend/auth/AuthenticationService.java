package com.portfolio.portfoliobackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.portfoliobackend.enums.TokenType;
import com.portfolio.portfoliobackend.models.Token;
import com.portfolio.portfoliobackend.repositories.TokenRepository;
import com.portfolio.portfoliobackend.security.jwt.JwtService;
import com.portfolio.portfoliobackend.enums.Role;
import com.portfolio.portfoliobackend.models.User;
import com.portfolio.portfoliobackend.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken, refreshToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken, refreshToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user){
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach((t) ->{
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String jwtToken, String refreshToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BAERER)
                .refreshToken(refreshToken)
                .expired(false)
                .revoked(false)
                .refreshTokenRevoked(false)
                .build();
        tokenRepository.save(token);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        var userToken = tokenRepository.findByRefreshTokenAndRefreshTokenRevokedIsFalse(refreshToken)
                .orElseThrow();

        if (!userEmail.isBlank() || !userToken.isRefreshTokenRevoked()){
            var userDetails = this.userRepository.findByEmail(userEmail).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, userDetails)){
                userToken.setRefreshTokenRevoked(true);
                tokenRepository.save(userToken);
                var accessToken = jwtService.generateToken(userDetails);
                revokeAllUserTokens(userDetails);
                saveUserToken(userDetails, accessToken, refreshToken);
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        return null;
    }
}
