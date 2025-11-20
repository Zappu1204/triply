package com.triply.tripapp.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalResult {
    
    @JsonProperty("position")
    private Integer position;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("place_id")
    private String placeId;
    
    @JsonProperty("data_id")
    private String dataId;
    
    @JsonProperty("data_cid")
    private String dataCid;
    
    @JsonProperty("reviews_link")
    private String reviewsLink;
    
    @JsonProperty("photos_link")
    private String photosLink;
    
    @JsonProperty("gps_coordinates")
    private GpsCoordinates gpsCoordinates;
    
    @JsonProperty("place_id_search")
    private String placeIdSearch;
    
    @JsonProperty("provider_id")
    private String providerId;
    
    @JsonProperty("rating")
    private Double rating;
    
    @JsonProperty("reviews")
    private Integer reviews;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("types")
    private List<String> types;
    
    @JsonProperty("type_id")
    private String typeId;
    
    @JsonProperty("type_ids")
    private List<String> typeIds;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("open_state")
    private String openState;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("thumbnail")
    private String thumbnail;
    
    @JsonProperty("serpapi_thumbnail")
    private String serpapiThumbnail;
}

