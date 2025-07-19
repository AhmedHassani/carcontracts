package com.ahd.backend.carcontracts.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
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
            String template = loadEmailTemplate();
            String content = String.format(
                    template, format.ownerName, format.companyUsername,
                    format.companyPassword, loginUri
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
            throw new RuntimeException("Failed to send email", e);
        }
    }


    private String loadEmailTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("email_template_ar.html");
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

}
