package com.portfolio.portfoliobackend.controllers.playground;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/playground")
public class PlaygroundController {

    @GetMapping
    public String get(){
        return "GET - user controller";
    }

    @PutMapping
    public String put(){
        return "PUT - user controller";
    }

    @PostMapping
    public String post(){
        return "POST - user controller";
    }

    @DeleteMapping
    public String delete(){
        return "DELETE - user controller";
    }

}
