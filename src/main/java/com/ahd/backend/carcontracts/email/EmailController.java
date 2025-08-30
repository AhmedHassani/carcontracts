package com.ahd.backend.carcontracts.email;

import com.ahd.backend.carcontracts.util.base.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("${application.api.base-path}/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ROLE_COMPANY')")
    public ApiResponse<?> sendEmail(@RequestBody Format format) {
        emailService.sendSimpleMail(format);
        return ApiResponse.builder()
                .success(true)
                .message("Email sent successfully!")
                .code(200)
                .date(Instant.now())
                .build();
    }
}
