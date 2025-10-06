package com.triply.tripapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String userName;

    @NotBlank
    private String password;
}






