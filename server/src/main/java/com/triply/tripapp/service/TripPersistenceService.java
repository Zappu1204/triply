package com.triply.tripapp.service;

import com.triply.tripapp.entity.Trip;
import com.triply.tripapp.entity.TripAttraction;
import com.triply.tripapp.entity.TripFlight;
import com.triply.tripapp.entity.TripHotel;
import com.triply.tripapp.repository.TripAttractionRepository;
import com.triply.tripapp.repository.TripFlightRepository;
import com.triply.tripapp.repository.TripHotelRepository;
import com.triply.tripapp.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripPersistenceService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripFlightRepository tripFlightRepository;

    @Autowired
    private TripHotelRepository tripHotelRepository;

    @Autowired
    private TripAttractionRepository tripAttractionRepository;

    public static class SaveFullRequest {
        public Integer tripId;
        public TripFlight flight;
        public TripHotel hotel;
        public List<TripAttraction> attractions;
    }

    @Transactional
    public Integer saveFullPlan(Integer tripId, TripFlight flight, TripHotel hotel, List<TripAttraction> attractions) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if (flight != null) {
            tripFlightRepository.findByTripId(tripId).ifPresent(existing -> tripFlightRepository.delete(existing));
            flight.setTripId(trip.getTripId());
            tripFlightRepository.save(flight);
        }

        if (hotel != null) {
            tripHotelRepository.findByTripId(tripId).ifPresent(existing -> tripHotelRepository.delete(existing));
            hotel.setTripId(trip.getTripId());
            tripHotelRepository.save(hotel);
        }

        if (attractions != null) {
            tripAttractionRepository.deleteByTripId(tripId);
            for (TripAttraction a : attractions) {
                a.setTripId(trip.getTripId());
                tripAttractionRepository.save(a);
            }
        }

        return trip.getTripId();
    }

    // CRUD Flight
    @Transactional
    public TripFlight upsertFlight(Integer tripId, TripFlight flight) {
        tripFlightRepository.findByTripId(tripId).ifPresent(existing -> flight.setTripFlightId(existing.getTripFlightId()));
        flight.setTripId(tripId);
        return tripFlightRepository.save(flight);
    }

    @Transactional
    public void deleteFlight(Integer tripId) {
        tripFlightRepository.findByTripId(tripId).ifPresent(tripFlightRepository::delete);
    }

    // CRUD Hotel
    @Transactional
    public TripHotel upsertHotel(Integer tripId, TripHotel hotel) {
        tripHotelRepository.findByTripId(tripId).ifPresent(existing -> hotel.setTripHotelId(existing.getTripHotelId()));
        hotel.setTripId(tripId);
        return tripHotelRepository.save(hotel);
    }

    @Transactional
    public void deleteHotel(Integer tripId) {
        tripHotelRepository.findByTripId(tripId).ifPresent(tripHotelRepository::delete);
    }

    // CRUD Attractions
    @Transactional
    public TripAttraction addAttraction(Integer tripId, TripAttraction attraction) {
        attraction.setTripId(tripId);
        return tripAttractionRepository.save(attraction);
    }

    @Transactional
    public void deleteAttraction(Integer attractionId) {
        tripAttractionRepository.deleteById(attractionId);
    }

    @Transactional
    public TripAttraction updateAttraction(TripAttraction attraction) {
        return tripAttractionRepository.save(attraction);
    }
}


