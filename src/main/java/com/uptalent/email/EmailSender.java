package com.uptalent.email;

import com.uptalent.email.model.EmailType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class EmailSender{
    private final JavaMailSender sender;
    public MimeMessageHelper chooseMessage(MimeMessageHelper helper, EmailType type, String fullname, LocalDateTime localDateTime, String token, String refererAddress) throws MessagingException {
        String message_body;
        String url;
        if(type == EmailType.RESTORE){
            helper.setSubject(EmailConstant.SUBJECT_RESTORE);
            url = refererAddress + "restore?token=" + token;
            message_body = """
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
                """;
        }
        else{
            helper.setSubject(EmailConstant.SUBJECT_VERIFY);
            url = refererAddress + "verify?token=" + token;
            message_body = """
                        Verify your account
                        </h1>
                        <h3 style="margin: 10px 0;">
                            Dear %s,
                        </h3>
                        <p>
                            Someone created an account with your email
                        </p>
                        <p>
                            If it wasn't you, then you don't need to do anything.
                        </p>
                        <p>
                            If it you, please activate it until <b>%s</b>
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
                            Verify your account
                        </button>
                        </a>
                    """;
        }
        helper.setText(EmailConstant.MESSAGE_BEGIN + message_body.formatted(fullname, localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE), url) + EmailConstant.MESSAGE_END, true);
        return helper;
    }
    @Async
    public void sendMail(String email, String token, String refererAddress, String fullname, LocalDateTime localDateTime, EmailType type) throws MessagingException {
        MimeMessage mail = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail);
        // String servletPath = request.getServletPath();
        helper = chooseMessage(helper, type, fullname, localDateTime, token, refererAddress);
        helper.setFrom(EmailConstant.ADMIN_MAIL);
        //helper.setTo(email);
        helper.setTo("uptalentinfo@gmail.com");
        try {
            sender.send(mail);
        }
        catch (MailException ex){
            log.error(ex.getMessage());
        }
    }
}
