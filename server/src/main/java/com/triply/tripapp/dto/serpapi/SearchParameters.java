package com.triply.tripapp.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchParameters {
    
    @JsonProperty("engine")
    private String engine;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("q")
    private String query;
    
    @JsonProperty("lat")
    private String latitude;
    
    @JsonProperty("lon")
    private String longitude;
    
    @JsonProperty("z")
    private String zoom;
    
    @JsonProperty("google_domain")
    private String googleDomain;
    
    @JsonProperty("hl")
    private String language;
    
    @JsonProperty("gl")
    private String country;
}

