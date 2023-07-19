package com.portfolio.portfoliobackend;

import com.portfolio.portfoliobackend.auth.AuthenticationService;
import com.portfolio.portfoliobackend.auth.RegisterRequest;
import com.portfolio.portfoliobackend.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PortfolioBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBackEndApplication.class, args);
	}

	@Bean
	public CommandLineRunner commnadLineRunner(
			AuthenticationService service
	){
		return args -> {
			var admin = RegisterRequest.builder()
					.name("admin")
					.email("admin@mail.com")
					.password("password")
					.role(Role.ADMIN)
					.build();
			System.out.println("\u001B[32m Admin token: "+service.register(admin).getAccessToken());

			var manager = RegisterRequest.builder()
					.name("user")
					.email("user@mail.com")
					.password("password")
					.role(Role.USER)
					.build();
			System.out.println("\u001B[32m User token: "+service.register(manager).getAccessToken());
		};
	}

}
