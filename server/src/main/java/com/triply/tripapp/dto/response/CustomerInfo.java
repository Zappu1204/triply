package com.triply.tripapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {
    private Integer customerId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String userName;
    private String socialProvider;
}






