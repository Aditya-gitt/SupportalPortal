package com.example.databaseDemo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Aliencontroller {

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String home() {
        return "home.jsp";
    }

}
