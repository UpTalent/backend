package com.uptalent.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
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

    public void sendMail(String email, String token, HttpServletRequest request, String fullname, LocalDateTime localDateTime) throws MessagingException {
        MimeMessage mail = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail);
        String refererAddress = request.getHeader(HttpHeaders.REFERER);
        // String servletPath = request.getServletPath();
        String url = refererAddress + "restore?token=" + token;
        helper.setFrom(EmailConstant.ADMIN_MAIL);
        helper.setTo(email);
        //helper.setTo("uptalentinfo@gmail.com");
        helper.setSubject(EmailConstant.SUBJECT);
        helper.setText("""
                        <div style="display: grid;\040
                        flex-direction: column;\040
                        border-radius: 10px;\040
                        padding: 20px;\040
                        font-size: larger;\040
                        border: 3px solid #48bde2;\040
                        margin: 3px;">
                        <h1 style="color: #48bde2;\040
                        margin: 0;">
                            Restore your account
                        </h1>
                        <h3 style="margin: 10px 0;">
                            Dear %s,
                        </h3>
                        <p>
                            We are sorry to hear that you have deleted your account, so to restore it no later than <b>%s</b>
                        </p>
                        <p>\040
                            Please click the button down bellow and follow the instructions.
                        </p>
                        <a href="%s" style="width: 200px; height: 50px; align-self: center;">
                        <button style="background: linear-gradient(87.27deg, rgb(156, 218, 237) 3.18%%, rgb(0, 147, 193) 63.05%%);
                        cursor: pointer;\040
                        color:white;\040
                        font-weight: bold;\040
                        width: 200px;\040
                        height: 50px;\040
                        border: none;\040
                        border-radius: 10px;">
                            Restore your account
                        </button>
                        </a>
                        <h4 style="margin-bottom: 0;">
                            Best regards,
                        </h4>
                        <div style="display: flex; align-items: center; gap: 5px;">
                        <img src="https://drive.google.com/uc?export=view&id=1Fx1E7h2r8ly23p5LCD5Jpc_slgUPSu2I" alt="logo" style="width:30px; object-fit: contain;"/>
                        <h4 style="margin-left: 5px;">
                            UPTALENT
                        </h4>
                        </div>
                        </div>
                """.formatted(fullname, localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), url), true);
        try {
            sender.send(mail);
        }
        catch (MailException ex){
            log.error(ex.getMessage());
        }
    }
}
