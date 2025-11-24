package com.triply.tripapp.repository;

import com.triply.tripapp.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Integer> {
    List<Destination> findByCityId(Integer cityId);
    Optional<Destination> findByPlaceId(String placeId);
    boolean existsByPlaceId(String placeId);
    Optional<Destination> findByName(String name);
}
