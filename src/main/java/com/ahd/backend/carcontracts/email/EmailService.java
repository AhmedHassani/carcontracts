package com.ahd.backend.carcontracts.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
@Slf4j

public class EmailService {

    @Value("${config.loginUri}")
    private String loginUri;
    @Value("${spring.mail.username}")
    private String email;
    @Value("${config.prod}")
    private String isProd;

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail(Format format) {
        try {
            String template = "<!DOCTYPE html>\n" +
                    "<html lang=\"ar\" dir=\"rtl\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>تفاصيل حساب الشركة</title>\n" +
                    "</head>\n" +
                    "<body style=\"font-family: 'Cairo', sans-serif; direction: rtl; line-height: 1.6;\">\n" +
                    "<h2>مرحبًا، %s!</h2>\n" +
                    "<p>تم إنشاء حساب شركتكم بنجاح.</p>\n" +
                    "<p><strong>تفاصيل الدخول:</strong></p>\n" +
                    "<ul>\n" +
                    "    <li><strong>اسم المستخدم:</strong> %s</li>\n" +
                    "    <li><strong>كلمة المرور:</strong> %s</li>\n" +
                    "</ul>\n" +
                    "<p>يرجى تغيير كلمة المرور عند تسجيل الدخول لأول مرة حفاظًا على الأمان.</p>\n" +
                    "<p>\n" +
                    "    <a href=\"%s\" style=\"padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;\">\n" +
                    "        تسجيل الدخول الآن\n" +
                    "    </a>\n" +
                    "</p>\n" +
                    "<p>إذا كان لديك أي استفسار أو تحتاج إلى مساعدة، لا تتردد في التواصل معنا.</p>\n" +
                    "<p>مع تحيات،<br>فريق الدعم</p>\n" +
                    "</body>\n" +
                    "</html>\n";
            String content = String.format(
                    template,
                    format.ownerName,
                    format.companyUsername,
                    format.companyPassword,
                    loginUri
            );
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(format.toEmail);
            if(format.cc!=null && format.cc.length != 0) {
                for (int i = 0 ; i < format.cc.length ; i++) {
                    helper.addCc(format.cc[i]);
                }
            }
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            //log.error(e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String loadEmailTemplate() throws IOException {
        Path path = new ClassPathResource("email_template_ar.html").getFile().toPath();
        return Files.readString(path, StandardCharsets.UTF_8);
    }

}
