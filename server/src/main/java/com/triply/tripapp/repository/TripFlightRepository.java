package com.triply.tripapp.repository;

import com.triply.tripapp.entity.TripFlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripFlightRepository extends JpaRepository<TripFlight, Integer> {
    Optional<TripFlight> findByTripId(Integer tripId);
}



