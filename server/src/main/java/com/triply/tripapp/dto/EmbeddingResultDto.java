package com.triply.tripapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResultDto {
    private Integer totalDestinations;
    private Integer successfullyEmbedded;
    private Integer failedEmbedding;
    private Integer skippedNoDescription;
    private String message;
}

