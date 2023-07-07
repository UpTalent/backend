package com.uptalent.util;

public class RegexValidation {
    public static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("^\\+?[0-9. ()-]{10,25}$");
    }

    public static boolean isValidTelegramUrl(String url){
        return url.matches("^(http|https)://t\\.me/[A-Za-z0-9_]{5,}$");
    }

    public static boolean isValidLinkedInUrl(String url){
        return url.matches("^(http|https)://(www\\.)?linkedin\\.com/.*$");
    }
}
