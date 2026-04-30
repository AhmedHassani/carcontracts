package com.ahd.backend.carcontracts.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${config.loginUri}")
    private String loginUri;

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail(Format format) {
        try {
            log.info("=== SENDING EMAIL ===");
            log.info("From: {}", fromEmail);
            log.info("To: {}", format.getToEmail());
            
            // Validate from email
            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new RuntimeException("From email is not configured in application.yml");
            }
            
            // Create HTML content
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <div style="background-color: #1a56db; color: white; padding: 20px; text-align: center;">
                        <h2>Welcome to Car Contracts System</h2>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear <strong>%s</strong>,</p>
                        <p>Your company account has been created successfully.</p>
                        <div style="background-color: #f3f4f6; padding: 15px; margin: 20px 0;">
                            <p><strong>Username:</strong> %s</p>
                            <p><strong>Password:</strong> %s</p>
                        </div>
                        <p>Please login at: <a href="%s">%s</a></p>
                        <p>Best regards,<br/>Car Contracts Team</p>
                    </div>
                </body>
                </html>
                """,
                format.getOwnerName(),
                format.getCompanyUsername(),
                format.getCompanyPassword(),
                loginUri,
                loginUri
            );
            
            // Create email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // CRITICAL: Set the from address explicitly
            helper.setFrom(fromEmail);
            helper.setTo(format.getToEmail().trim());
            helper.setSubject("Your Car Contracts Company Account Credentials");
            helper.setText(htmlContent, true);
            
            // Add CC if present
            if (format.getCc() != null && format.getCc().length > 0) {
                for (String cc : format.getCc()) {
                    if (cc != null && !cc.trim().isEmpty()) {
                        helper.addCc(cc.trim());
                    }
                }
            }
            
            log.info("Sending email...");
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", format.getToEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}