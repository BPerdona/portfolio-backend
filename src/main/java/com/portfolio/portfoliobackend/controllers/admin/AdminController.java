package com.portfolio.portfoliobackend.controllers.admin;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping
    public String get(){
        return "GET - admin controller";
    }

    @PutMapping
    public String put(){
        return "PUT - admin controller";
    }

    @PostMapping
    public String post(){
        return "POST - admin controller";
    }

    @DeleteMapping
    public String delete(){
        return "DELETE - admin controller";
    }
}
