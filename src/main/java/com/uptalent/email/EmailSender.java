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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSender{
    private final JavaMailSender sender;

    public void sendMail(String email, String token, HttpServletRequest request, String fullname, LocalDateTime localDateTime){
        SimpleMailMessage msg = new SimpleMailMessage();
        String refererAddress = request.getHeader(HttpHeaders.REFERER);
        String servletPath = request.getServletPath();
        msg.setFrom(EmailConstant.ADMIN_MAIL);
        //msg.setTo("uptalentinfo@gmail.com");
        msg.setSubject(EmailConstant.SUBJECT);
        String url = refererAddress + servletPath.substring(1) + "restore?token=" + token;
        msg.setTo(email);
        msg.setText("""
                       Dear %s,
                       We are sorry to hear that you have deleted your account,\040
                       so to restore it no later than %s, please follow the link\040
                       %s\040
                       and click on the restore account button.\040
                       Best regards,
                       UPTALENT
                """.formatted(fullname, localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), url));
        try {
            sender.send(msg);
        }
        catch (MailException ex){
            log.error(ex.getMessage());
        }
    }
}
