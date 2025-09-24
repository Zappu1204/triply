package com.triply.tripapp.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SocialLoginRequest {
    @NotBlank
    private String provider; // GOOGLE

    @NotBlank
    private String token; // Google ID Token
}






