package com.triply.tripapp.repository;

import com.triply.tripapp.entity.TripAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripAttractionRepository extends JpaRepository<TripAttraction, Integer> {
    List<TripAttraction> findByTripId(Integer tripId);
    void deleteByTripId(Integer tripId);
}



