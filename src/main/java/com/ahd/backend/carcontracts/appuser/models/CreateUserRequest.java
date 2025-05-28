package com.ahd.backend.carcontracts.appuser.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one digit, one uppercase, one lowercase letter and one special character"
    )
    private String password;

    @NotBlank(message = "Email must notAuthorization:Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzdXBlciIsImlhdCI6MTc0NzQxMTg4NCwiZXhwIjoxNzUwMDAzODg0fQ.5I0PJ8XBEJl96eKhdfYWOXKtj3lpmgS0Smnc4SkBjZWeeiwPpn3WKiqCzd1OqvCrQ0CKdX4I--ag2Qe0CgXITQ be blank")
    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    private String image;

    private Set<Long> roleIds;
} 