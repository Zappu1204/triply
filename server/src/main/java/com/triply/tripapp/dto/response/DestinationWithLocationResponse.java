package com.triply.tripapp.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationWithLocationResponse {
    private Integer destinationId;
    private String name;
    private String address;
    private String imgPath;
    private String googleMapUrl;
    private String description;
    private Integer cityId;
    private String placeId;
    private Double rating;
    private Integer reviewCount;
    private String types;
    private String website;
    private String openState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Location fields
    private Double latitude;
    private Double longitude;
}
