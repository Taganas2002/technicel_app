package com.technicel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/dashboard",
            "/progress",
            "/day-off",
            "/profile",
            "/lists/**",
            "/admin/**"
    })
    public String frontend() {
        return "forward:/index.html";
    }
}
