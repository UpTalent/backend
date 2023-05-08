package com.uptalent.email;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSender{
    private final JavaMailSender sender;

    public void sendMail(String email, String token, HttpServletRequest request){
        SimpleMailMessage msg = new SimpleMailMessage();
        String url_address = request.getRequestURL().toString();
        msg.setFrom(EmailConstant.ADMIN_MAIL);
        msg.setTo("uptalentinfo@gmail.com");
        //msg.setTo(email);
        msg.setSubject(EmailConstant.SUBJECT);
        msg.setText(EmailConstant.DELETE_MESSAGE + '\n' + url_address + "/restore?token=" + token);
        try {
            sender.send(msg);
        }
        catch (MailException ex){
            log.error(ex.getMessage());
        }
    }
}
