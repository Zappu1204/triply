package com.triply.tripapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_Destination")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "destinationId")
    private Integer destinationId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "imgPath", length = 500)
    private String imgPath;

    @Column(name = "googleMapUrl", length = 500)
    private String googleMapUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tbl_CitycityId")
    private Integer cityId;

    // New fields from SerpAPI
    @Column(name = "placeId", length = 255)
    private String placeId;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "reviewCount")
    private Integer reviewCount;

    @Column(name = "types", length = 500)
    private String types;  // Comma-separated type IDs

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "openState", length = 255)
    private String openState;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
