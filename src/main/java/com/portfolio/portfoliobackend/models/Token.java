package com.portfolio.portfoliobackend.models;

import com.portfolio.portfoliobackend.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Token")
public class Token {

    @Id
    @GeneratedValue
    private Long id;
    private String token;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    private boolean expired;
    private boolean revoked;
    @Column(name = "refresh_token_revoked")
    private boolean refreshTokenRevoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
