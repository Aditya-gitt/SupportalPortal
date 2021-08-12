package com.example.demo.listner;

import com.example.demo.domain.MyUserDetails;
import com.example.demo.domain.User;
import com.example.demo.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class AuthenticationSuccessListner {
    private LoginAttemptService loginAttemptService;

    @Autowired
    public AuthenticationSuccessListner(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        MyUserDetails user = (MyUserDetails) event.getAuthentication().getPrincipal();
        this.loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
    }
}
