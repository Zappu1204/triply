package com.triply.tripapp.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMapsResponse {
    
    @JsonProperty("search_metadata")
    private SearchMetadata searchMetadata;
    
    @JsonProperty("search_parameters")
    private SearchParameters searchParameters;
    
    @JsonProperty("local_results")
    private List<LocalResult> localResults;
    
    @JsonProperty("serpapi_pagination")
    private SerpapiPagination serpapiPagination;
}

