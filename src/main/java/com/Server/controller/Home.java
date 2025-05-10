package com.Server.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Home {
    @GetMapping
    public String home() {
        return "Facebook Server";
    }
}
