package com.ahd.backend.carcontracts.email;

import com.ahd.backend.carcontracts.util.base.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${application.api.base-path}/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ApiResponse<String> sendEmail(@RequestBody Format format) {
        emailService.sendSimpleMail(format);
        return new ApiResponse<>(true,"Email sent successfully!",200,null);
    }
}
