package com.example.demo.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token can't be verified";
    public static final String AKI_LLC = "AKI, LLC";
    public static final String AKI_ADMINISTRATION = "User Management Portal";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to get the access to this page!!";
    public static final String ACCESS_DENIED_MESSAGE = "You don't have permission to access this page!!";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS = {"/user/login","/user/registration", "/user/resetpassword/**", "/user/image/**" };
}
