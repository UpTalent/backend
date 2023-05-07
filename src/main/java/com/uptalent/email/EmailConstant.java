package com.uptalent.email;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConstant {

    @Value("${spring.mail.username}")
    private String email;
    public static String ADMIN_MAIL;
    public static final String SUBJECT = "Your UpTalent account was temporary deleted";

    public static final String DELETE_MESSAGE = "Your account will be permanent deleted in 7 days, if you want to restore it follow this link: ";

    //public static final String FRONT_ADDRESS = "http://dev.uptalent.pepega.cloud/api/v1/sponsors/";

    @Value("${spring.mail.username}")
    private void setAdminMail(){
        EmailConstant.ADMIN_MAIL = this.email;
    }

}
