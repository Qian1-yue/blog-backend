package com.example.blogbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(
            @RequestParam(name = "name", defaultValue = "Spring Boot")
            String name) {
        return "Hello, " + name;
    }
}
