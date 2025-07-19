package com.ahd.backend.carcontracts.email;

import com.ahd.backend.carcontracts.util.base.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("${application.api.base-path}/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
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
