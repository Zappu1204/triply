package com.triply.tripapp.repository;

import com.triply.tripapp.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Integer> {
    Optional<Destination> findByName(String name);
}



