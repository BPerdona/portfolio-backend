package com.portfolio.portfoliobackend;

import com.portfolio.portfoliobackend.auth.AuthenticationResponse;
import com.portfolio.portfoliobackend.auth.AuthenticationService;
import com.portfolio.portfoliobackend.auth.RegisterRequest;
import com.portfolio.portfoliobackend.enums.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PortfolioBackEndApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class PermissionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthenticationService service;

    private static String userToken;

    private static String adminToken;

    private static boolean initialized = false;

    @Before
    public void registerUser(){
        if (!initialized){
            final RegisterRequest mockUser =
                    new RegisterRequest("John", "john@gmail.com", "password123", Role.USER);
            userToken = service.register(mockUser).getAccessToken();

            final RegisterRequest mockAdmin =
                    new RegisterRequest("Admin", "admin@gmail.com", "password123", Role.ADMIN);
            adminToken = service.register(mockAdmin).getAccessToken();

            initialized = true;
        }
    }

    @Test
    public void permissionWorksProperlyForGet(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.GET,
                createHttpEntity(userToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void permissionWorksProperlyForPost(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.POST,
                createHttpEntity(userToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void permissionWorksProperlyForPut(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.PUT,
                createHttpEntity(userToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void permissionWorksProperlyForDelete(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.DELETE,
                createHttpEntity(userToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldReturnForbiddenWithoutAuth(){
        var response = restTemplate.getForEntity(
                "/api/v1/playground",
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldReturnForbiddenWithEmptyAuth(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.GET,
                createHttpEntity(" "),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldReturnForbiddenWithWrongPermission(){
        var response = restTemplate.exchange(
                "/api/v1/admin",
                HttpMethod.GET,
                createHttpEntity(" "),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void adminShouldReturnOk(){
        var response = restTemplate.exchange(
                "/api/v1/admin",
                HttpMethod.GET,
                createHttpEntity(adminToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void adminShouldReturnOkInUserPathGet(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.GET,
                createHttpEntity(adminToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void adminShouldReturnOkInUserPathPost(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.POST,
                createHttpEntity(adminToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void adminShouldReturnOkInUserPathPut(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.PUT,
                createHttpEntity(adminToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void adminShouldReturnOkInUserPathDelete(){
        var response = restTemplate.exchange(
                "/api/v1/playground",
                HttpMethod.DELETE,
                createHttpEntity(adminToken),
                String.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpEntity<String> createHttpEntity(String token){
        if (token == null){
            token = "";
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer "+token);
        return new HttpEntity<>("", header);
    }

}
