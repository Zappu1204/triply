package com.triply.tripapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResultDto {
    private Integer totalCities;
    private Integer totalDestinationsFound;
    private Integer newDestinationsAdded;
    private Integer existingDestinationsSkipped;
    private String message;
}

