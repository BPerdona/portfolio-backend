package com.portfolio.portfoliobackend;

import com.portfolio.portfoliobackend.auth.*;
import com.portfolio.portfoliobackend.enums.Role;
import com.portfolio.portfoliobackend.models.Token;
import com.portfolio.portfoliobackend.models.User;
import com.portfolio.portfoliobackend.repositories.TokenRepository;
import com.portfolio.portfoliobackend.repositories.UserRepository;
import com.portfolio.portfoliobackend.security.jwt.JwtService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.List;

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
            new RegisterRequest("John", "john@gmail.com", "password123");

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

        User repUser = userRepository.findByEmail(mockUser.getEmail()).get();
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
        userRepository.delete(userRepository.findByEmail(mockUser.getEmail()).orElseThrow());
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
        RegisterRequest emptyRequest = new RegisterRequest("","","");
        ResponseEntity<AuthenticationResponse> emptyResponse = restTemplate.postForEntity(
                BASE_URL+"/register",
                emptyRequest,
                AuthenticationResponse.class
        );
        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerWithSpacesValuesShouldReturnBadRequest() {
        RegisterRequest spacesRequest = new RegisterRequest("   ","   ","   ");
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
        userRepository.delete(userRepository.findByEmail(mockUser.getEmail()).orElseThrow());
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
    @DirtiesContext
    public void logoutProperly(){
        var token = restTemplate.postForEntity(
                BASE_URL+"/register",
                mockUser,
                AuthenticationResponse.class
        ).getBody().getToken();

        assertThat(token).isNotNull();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer "+token);

        HttpEntity<String> entity = new HttpEntity<>("",header);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                entity,
                String.class
        );

        Token mockUserToken = tokenRepository.findByToken(token).orElseThrow();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        assertThat(mockUserToken.isExpired()).isTrue();
        assertThat(mockUserToken.isRevoked()).isTrue();
    }

    @Test
    public void logoutWithAWrongTokenReturnBadRequest(){
        String randomToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJicnVub0BnbWFpbC5jb20iL" +
                "CJpYXQiOjE2ODcxMzU0NTksImV4cCI6MTY4NzEzODMzOX0.fVb4oAhvgsY4aNW0KOWCUm" +
                "Ed5guK5v-vl800qHTv5DM";

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer "+randomToken);

        HttpEntity<String> entity = new HttpEntity<>("",header);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void logoutWithAEmptyTokenReturnBadRequest(){
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer ");

        HttpEntity<String> entity = new HttpEntity<>("",header);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL+"/logout",
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
