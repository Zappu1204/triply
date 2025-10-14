package com.triply.tripapp.repository;

import com.triply.tripapp.entity.TripHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripHotelRepository extends JpaRepository<TripHotel, Integer> {
    Optional<TripHotel> findByTripId(Integer tripId);
}


