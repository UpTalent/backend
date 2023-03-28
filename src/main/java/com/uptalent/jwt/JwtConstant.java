package com.uptalent.jwt;

public class JwtConstant {
    public static final long EXPIRATION_TIME = 3600 * 1000; // 1-hour
    public static final String TOKEN_HEADER = "Bearer ";
    public static final String JWT_TOKEN_HEADER_NAME = "Jwt-Token";
    public static final String TOKEN_ISSUE = "UpTalent";
    public static final String TOKEN_NOT_VERIFIED_MESSAGE = "The token cannot be verified";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String ROLE_CLAIM = "Role";
    public static final String FIRSTNAME_CLAIM = "Firstname";

}
