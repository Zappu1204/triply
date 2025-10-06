package com.triply.tripapp.repository;

import com.triply.tripapp.entity.ItineraryItem;
import com.triply.tripapp.entity.ItineraryItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, ItineraryItemId> {
    List<ItineraryItem> findByTripId(Integer tripId);
}



