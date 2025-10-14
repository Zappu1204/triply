package com.triply.tripapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_TripHotel")
public class TripHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripHotelId")
    private Integer tripHotelId;

    @Column(name = "tbl_TriptripId", nullable = false, unique = true)
    private Integer tripId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "priceTotalVnd")
    private Integer priceTotalVnd;

    public Integer getTripHotelId() {
        return tripHotelId;
    }

    public void setTripHotelId(Integer tripHotelId) {
        this.tripHotelId = tripHotelId;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getPriceTotalVnd() {
        return priceTotalVnd;
    }

    public void setPriceTotalVnd(Integer priceTotalVnd) {
        this.priceTotalVnd = priceTotalVnd;
    }
}


