package com.portfolio.portfoliobackend.auth;

import com.portfolio.portfoliobackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final UserRepository repository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){

        if (repository.findByEmail(request.getEmail()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        if(!isUserInputValid(request.getEmail(), request.getPassword()))
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    private boolean isUserInputValid(String email, String password){
        if(email == null || password == null){
            return false;
        }
        if (email.isBlank() || password.isBlank() || password.length()<=7)
            return false;
        return true;
    }
}
