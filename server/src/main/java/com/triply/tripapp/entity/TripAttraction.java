package com.triply.tripapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_TripAttraction")
public class TripAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripAttractionId")
    private Integer tripAttractionId;

    @Column(name = "tbl_TriptripId", nullable = false)
    private Integer tripId;

    @Column(name = "date", length = 20)
    private String date;

    @Column(name = "time", length = 20)
    private String time;

    @Column(name = "activity", length = 255)
    private String activity;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "reason", length = 500)
    private String reason;

    public Integer getTripAttractionId() { return tripAttractionId; }
    public void setTripAttractionId(Integer tripAttractionId) { this.tripAttractionId = tripAttractionId; }
    public Integer getTripId() { return tripId; }
    public void setTripId(Integer tripId) { this.tripId = tripId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}



