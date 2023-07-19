package com.portfolio.portfoliobackend;

import com.portfolio.portfoliobackend.auth.*;
import com.portfolio.portfoliobackend.enums.Role;
import com.portfolio.portfoliobackend.models.Token;
import com.portfolio.portfoliobackend.models.User;
import com.portfolio.portfoliobackend.repositories.TokenRepository;
import com.portfolio.portfoliobackend.repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PortfolioBackEndApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class JwtAuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    private final String BASE_URL = "/api/v1/auth";


    private final RegisterRequest mockUser =
            new RegisterRequest("John", "john@gmail.com", "password123", Role.USER);

    // Registration
    @Test
    @DirtiesContext
    public void registrationWorksThroughAllLayer(){
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        User repUser = userRepository.findByEmail(mockUser.getEmail()).orElseThrow();
        assertThat(repUser).isNotNull();
        assertThat(repUser.getEmail()).isEqualTo(mockUser.getEmail());
        assertThat(repUser.getName()).isEqualTo(mockUser.getName());
        assertThat(repUser.getRole()).isEqualTo(Role.USER);
        assertThat(repUser.getPassword()).isNotNull();
    }

    @Test
    @DirtiesContext
    public void registerAExistingUserShouldReturnConflict(){
        restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );

        ResponseEntity<AuthenticationResponse> repeatResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );

        assertThat(repeatResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        deleteMock();
    }

    @Test
    public void registerWithAEmptyBodyReturnForbidden(){
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                BASE_URL+"/register",
                "",
                AuthenticationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void registerWithNulllValuesShouldReturnBadRequest(){
        RegisterRequest nullRequest = new RegisterRequest();
        ResponseEntity<AuthenticationResponse> nullResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                nullRequest,
                AuthenticationResponse.class
        );
        assertThat(nullResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerWithEmptyValuesShouldReturnBadRequest(){
        RegisterRequest emptyRequest = new RegisterRequest("","","", null);
        ResponseEntity<AuthenticationResponse> emptyResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                emptyRequest,
                AuthenticationResponse.class
        );
        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerWithSpacesValuesShouldReturnBadRequest() {
        RegisterRequest spacesRequest = new RegisterRequest("   ","   ","   ", null);
        ResponseEntity<AuthenticationResponse> spacesResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                spacesRequest,
                AuthenticationResponse.class
        );
        assertThat(spacesResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Authentication
    @Test
    @DirtiesContext
    public void authenticateWorksProperly(){
        restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );
        AuthenticationRequest authRequest = new AuthenticationRequest("john@gmail.com", "password123");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authResponse.getBody()).isNotNull();
        deleteMock();
    }

    @Test
    public void authenticateUnknownUserReturnsForbidden(){
        AuthenticationRequest authRequest = new AuthenticationRequest("unknowUser@test.com", "password");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(authResponse.getBody()).isNull();
    }

    @Test
    public void authenticateWithWrongPasswordReturnsForbidden(){
        restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );
        AuthenticationRequest authRequest = new AuthenticationRequest("john@gmail.com", "WrongPass123");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(authResponse.getBody()).isEqualTo(null);
        deleteMock();
    }

    @Test
    public void authenticateWithEmptyBodyReturnsForbidden(){
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                null,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(authResponse.getBody()).isEqualTo(null);
    }

    @Test
    public void authenticateWithEmptyValuesReturnsForbidden(){
        AuthenticationRequest authRequest = new AuthenticationRequest("", "");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(authResponse.getBody()).isEqualTo(null);
    }

    @Test
    public void authenticateWithSpacesValuesReturnsForbidden(){
        AuthenticationRequest authRequest = new AuthenticationRequest("", "");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                BASE_URL+"/authenticate",
                authRequest,
                AuthenticationResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(authResponse.getBody()).isEqualTo(null);
    }

    // Logout
    @Test
    public void logoutProperly(){
        var registerResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );

        assertThat(registerResponse).isNotNull();
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResponse.getBody()).isNotNull();

        var token = registerResponse.getBody().getAccessToken();
        assertThat(token).isNotNull();

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                createHttpEntity(token),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        Token mockUserToken = tokenRepository.findByToken(token).orElseThrow();
        assertThat(mockUserToken.isExpired()).isTrue();
        assertThat(mockUserToken.isRevoked()).isTrue();
        deleteMock();
    }

    @Test
    public void logoutWithAWrongTokenReturnBadRequest(){
        String randomToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJicnVub0BnbWFpbC5jb20iL" +
                "CJpYXQiOjE2ODcxMzU0NTksImV4cCI6MTY4NzEzODMzOX0.fVb4oAhvgsY4aNW0KOWCUm" +
                "Ed5guK5v-vl800qHTv5DM";

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                createHttpEntity(randomToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void logoutWithAEmptyTokenReturnBadRequest(){
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                createHttpEntity(""),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Refresh Token
    @Test
    public void basicRefreshTokenCycle(){
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var token = tokenRepository.findByToken(response.getBody().getAccessToken()).orElseThrow();
        token.setRevoked(true);
        token.setExpired(true);
        tokenRepository.save(token);

        var refreshResponse = restTemplate.postForEntity(
                BASE_URL+"/refresh-token",
                createHttpEntity(token.getRefreshToken()),
                AuthenticationResponse.class
        );

        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        var responseToken = refreshResponse.getBody();
        assert responseToken != null;
        assertThat(responseToken.getRefreshToken()).isEqualTo(token.getRefreshToken());
        assertThat(responseToken.getAccessToken()).isNotBlank();

        var newToken = tokenRepository.findByRefreshTokenAndRefreshTokenRevokedIsFalse(responseToken.getRefreshToken()).orElseThrow();
        assertThat(newToken.isExpired()).isFalse();
        assertThat(newToken.isRevoked()).isFalse();
        assertThat(newToken.isRefreshTokenRevoked()).isFalse();

        deleteMock();
    }

    @Test
    public void refreshTokenWithoutTokenReturnForbidden(){
        var refreshResponse = restTemplate.postForEntity(
                BASE_URL+"/refresh-token",
                createHttpEntity(""),
                AuthenticationResponse.class
        );

        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(refreshResponse.getBody()).isNull();
    }

    @Test
    public void refreshTokenWithLogoutUserReturnsForbidden(){
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var token = response.getBody().getAccessToken();
        ResponseEntity<String> logoutResponse = restTemplate.postForEntity(
                BASE_URL+"/logout",
                createHttpEntity(token),
                String.class
        );

        assertThat(logoutResponse).isNotNull();
        assertThat(logoutResponse.getBody()).isNull();
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        var refreshToken = response.getBody().getRefreshToken();
        var refreshResponse = restTemplate.postForEntity(
                BASE_URL+"/refresh-token",
                createHttpEntity(refreshToken),
                AuthenticationResponse.class
        );

        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(refreshResponse.getBody()).isNull();
    }


    public HttpEntity<String> createHttpEntity(String token){
        if (token == null){
            token = "";
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer "+token);
        return new HttpEntity<>("", header);
    }

    public void deleteMock(){
        userRepository.delete(userRepository.findByEmail(mockUser.getEmail()).orElseThrow());
    }
}
