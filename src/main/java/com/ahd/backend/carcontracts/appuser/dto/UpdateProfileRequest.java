package com.ahd.backend.carcontracts.appuser.dto;

import com.ahd.backend.carcontracts.util.annotation.AtLeastOneFieldNotNull;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@AtLeastOneFieldNotNull
public class UpdateProfileRequest {

    @Size(max = 50, message = "Full name must not exceed 50 characters")
    @Pattern(
            regexp = "^[\\p{L} ]+$",
            message = "Full name must contain only letters and spaces (no special characters)"
    )
    private String fullName;

    @Email(message = "Email must be a valid address")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone must consist of 10–15 digits (optionally starting with ‘+’)"
    )
    private String phone;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "must be at least 8 characters long and contain at least one digit, one uppercase, one lowercase letter and one special character"
    )
    private String password;
}