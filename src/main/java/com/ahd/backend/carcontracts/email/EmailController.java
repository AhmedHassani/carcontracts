package com.ahd.backend.carcontracts.email;

import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("${application.api.base-path}/email")
@Slf4j
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ROLE_COMPANY')")
    public ApiResponse<?> sendEmail(@RequestBody Format format) {
        try {
            log.info("Received email request for: {}", format.getToEmail());
            emailService.sendSimpleMail(format);
            return ApiResponse.builder()
                    .success(true)
                    .message("Email sent successfully!")
                    .code(200)
                    .date(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("Email sending failed", e);
            return ApiResponse.builder()
                    .success(false)
                    .message("Failed to send email: " + e.getMessage())
                    .code(500)
                    .date(Instant.now())
                    .build();
        }
    }
    
    // @PostMapping("/test")
    // public ApiResponse<?> testEmail() {
    //     try {
    //         Format format = Format.builder()
    //                 .toEmail("alisafaa.ve911@gmail.com")
    //                 .ownerName("علي الشركة")
    //                 .companyUsername("شركة_علي")
    //                 .companyPassword("AliCompany123")
    //                 .build();
            
    //         emailService.sendSimpleMail(format);
            
    //         return ApiResponse.builder()
    //                 .success(true)
    //                 .message("Test email sent successfully!")
    //                 .code(200)
    //                 .date(Instant.now())
    //                 .build();
    //     } catch (Exception e) {
    //         log.error("Test email failed", e);
    //         return ApiResponse.builder()
    //                 .success(false)
    //                 .message("Test failed: " + e.getMessage())
    //                 .code(500)
    //                 .date(Instant.now())
    //                 .build();
    //     }
    // }
}