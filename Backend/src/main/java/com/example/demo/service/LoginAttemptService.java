package com.example.demo.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    public static final int MAX_NUMBER_OF_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    private LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        this.loginAttemptCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, Integer>() {
                            @Override
                            public Integer load(String s) throws Exception {
                                return null;
                            }
                        });

    }

    public void evictUserFromLoginAttemptCache(String username) {
        this.loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username){
        int attempts = 0;
        try {
            attempts = ATTEMPT_INCREMENT + this.loginAttemptCache.get(username);
        } catch (Exception e) {
            e.printStackTrace();
            attempts = 1;
        }
        this.loginAttemptCache.put(username, attempts);
    }

    public boolean hasExceededMaxAttempts(String username) {
        try {
            return this.loginAttemptCache.get(username) >= MAX_NUMBER_OF_ATTEMPTS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
