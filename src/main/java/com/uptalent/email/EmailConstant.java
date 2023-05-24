package com.uptalent.email;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConstant {

    @Value("${spring.mail.username}")
    private String email;
    public static String ADMIN_MAIL;
    public static final String SUBJECT_RESTORE = "Your UpTalent account was temporary deleted";
    public static final String SUBJECT_VERIFY = "Your UPTalent account not activated";
    public static final String MESSAGE_BEGIN = """
                    <div style="display: grid;\040
                        flex-direction: column;\040
                        border-radius: 10px;\040
                        padding: 20px;\040
                        font-size: larger;\040
                        border: 3px solid #48bde2;\040
                        margin: 3px;">
                        <h1 style="color: #48bde2;\040
                        margin: 0;">""";
    public static final String MESSAGE_END = """
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
                """;

    //public static final String FRONT_ADDRESS = "http://dev.uptalent.pepega.cloud/api/v1/sponsors/";

    @Value("${spring.mail.username}")
    private void setAdminMail(){
        EmailConstant.ADMIN_MAIL = this.email;
    }



}
