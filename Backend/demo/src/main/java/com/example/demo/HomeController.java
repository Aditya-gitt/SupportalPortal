package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;


@RestController
public class HomeController {

    @RequestMapping("/Home")
    public String home(HttpServletRequest req) {
        String name = req.getParameter("name");
        return("Hello" + name);
    }

}
