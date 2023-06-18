package com.portfolio.portfoliobackend;

import com.portfolio.portfoliobackend.auth.AuthenticationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PortfolioBackEndApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class SmokeTest {

    @Autowired
    private AuthenticationController controller;

    // TODO: Add all controllers, beans and Services
    @Test
    public void contextLoads() throws Exception{
        assertThat(controller).isNotNull();
    }
}
