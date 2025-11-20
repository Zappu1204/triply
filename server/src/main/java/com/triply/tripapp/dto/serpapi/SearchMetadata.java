package com.triply.tripapp.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchMetadata {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("processed_at")
    private String processedAt;
    
    @JsonProperty("google_maps_url")
    private String googleMapsUrl;
    
    @JsonProperty("total_time_taken")
    private Double totalTimeTaken;
}

