package com.ahd.backend.carcontracts.appuser.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdatePhoneRequest {
    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in international format (E.164)")
    private String phone;
} 