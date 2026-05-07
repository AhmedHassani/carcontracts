package com.ahd.backend.carcontracts.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {

    private static final String ARABIC_TEMPLATE_PATH = "email_template_ar.html";
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${config.loginUri}")
    private String loginUri;

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail(Format format) {
        try {
            log.info("📧 Sending email to: {}", format.getToEmail());
            
            // Load Arabic template
            String htmlContent = loadArabicTemplate(
                format.getOwnerName(),
                format.getCompanyUsername(),
                format.getCompanyPassword(),
                loginUri
            );
            
            String subject = "تفاصيل حساب شركتك في نظام عقود السيارات";
            
            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(format.getToEmail().trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Add CC if present
            if (format.getCc() != null && format.getCc().length > 0) {
                for (String cc : format.getCc()) {
                    if (cc != null && !cc.trim().isEmpty()) {
                        helper.addCc(cc.trim());
                    }
                }
            }
            
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", format.getToEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    private String loadArabicTemplate(String ownerName, String username, String password, String loginUrl) {
        try {
            ClassPathResource resource = new ClassPathResource(ARABIC_TEMPLATE_PATH);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return String.format(template, ownerName, username, password, loginUrl);
        } catch (Exception e) {
            log.error("Failed to load Arabic template, using fallback", e);
            return generateFallbackArabic(ownerName, username, password, loginUrl);
        }
    }
    
    private String generateFallbackArabic(String ownerName, String username, String password, String loginUrl) {
        return String.format("""
            <html dir="rtl">
            <body style="font-family: 'Cairo', sans-serif;">
                <h2>مرحبًا، %s!</h2>
                <p>تم إنشاء حساب شركتكم بنجاح.</p>
                <p><strong>تفاصيل الدخول:</strong></p>
                <ul>
                    <li><strong>اسم المستخدم:</strong> %s</li>
                    <li><strong>كلمة المرور:</strong> %s</li>
                </ul>
                <p><a href="%s">تسجيل الدخول</a></p>
            </body>
            </html>
            """, ownerName, username, password, loginUrl);
    }
}