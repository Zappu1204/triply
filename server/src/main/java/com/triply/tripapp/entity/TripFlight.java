package com.triply.tripapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_TripFlight")
public class TripFlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripFlightId")
    private Integer tripFlightId;

    @Column(name = "tbl_TriptripId", nullable = false, unique = true)
    private Integer tripId;

    @Column(name = "departureId", length = 10)
    private String departureId;

    @Column(name = "arrivalId", length = 10)
    private String arrivalId;

    @Column(name = "airline", length = 100)
    private String airline;

    @Column(name = "airlineId", length = 10)
    private String airlineId;

    @Column(name = "priceVnd")
    private Integer priceVnd;

    @Column(name = "flightDuration", length = 50)
    private String flightDuration;

    @Column(name = "departureTime", length = 30)
    private String departureTime;

    @Column(name = "arrivalTime", length = 30)
    private String arrivalTime;

    public Integer getTripFlightId() {
        return tripFlightId;
    }

    public void setTripFlightId(Integer tripFlightId) {
        this.tripFlightId = tripFlightId;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public String getDepartureId() {
        return departureId;
    }

    public void setDepartureId(String departureId) {
        this.departureId = departureId;
    }

    public String getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(String arrivalId) {
        this.arrivalId = arrivalId;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    public Integer getPriceVnd() {
        return priceVnd;
    }

    public void setPriceVnd(Integer priceVnd) {
        this.priceVnd = priceVnd;
    }

    public String getFlightDuration() {
        return flightDuration;
    }

    public void setFlightDuration(String flightDuration) {
        this.flightDuration = flightDuration;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}



