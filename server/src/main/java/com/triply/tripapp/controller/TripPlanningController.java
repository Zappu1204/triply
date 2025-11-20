package com.triply.tripapp.controller;

import com.triply.tripapp.entity.Expense;
import com.triply.tripapp.entity.ItineraryItem;
import com.triply.tripapp.service.TripPlanningService.PlanRequest;
import com.triply.tripapp.service.TripPlanningService.PlanResponse;
import com.triply.tripapp.service.TripPlanningService;
import com.triply.tripapp.entity.TripFlight;
import com.triply.tripapp.entity.TripHotel;
import com.triply.tripapp.entity.TripAttraction;
import com.triply.tripapp.service.TripPersistenceService;
import com.triply.tripapp.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trip")
public class TripPlanningController {

    public static class SaveRequest {
        public String title;
        public LocalDate startDate;
        public LocalDate endDate;
        public BigDecimal totalBudget;
        public String currency;
        public List<ItineraryItem> items;
        public List<Expense> estimatedExpenses;
    }

    @Autowired
    private TripPlanningService tripPlanningService;

    @Autowired
    private TripPersistenceService tripPersistenceService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/plan")
    public ResponseEntity<PlanResponse> plan(@RequestBody PlanRequest request) throws Exception {
        return ResponseEntity.ok(tripPlanningService.plan(request));
    }

    @PostMapping("/save")
    public ResponseEntity<Integer> save(Authentication authentication, @RequestBody SaveRequest req) {
        Integer customerId = authUtil.getCustomerId(authentication);
        return ResponseEntity.ok(tripPlanningService
                .savePlannedTrip(customerId, req.title, req.startDate, req.endDate, req.totalBudget, req.currency, req.items, req.estimatedExpenses)
                .getTripId());
    }

    public static class SaveFullPlanRequest {
        public Integer tripId;
        public TripFlight flight;
        public TripHotel hotel;
        public List<TripAttraction> attractions;
    }

    @PostMapping("/save-full")
    public ResponseEntity<Integer> saveFull(@RequestBody SaveFullPlanRequest req) {
        Integer id = tripPersistenceService.saveFullPlan(req.tripId, req.flight, req.hotel, req.attractions);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/{tripId}/flight")
    public ResponseEntity<TripFlight> upsertFlight(@org.springframework.web.bind.annotation.PathVariable Integer tripId,
                                                   @RequestBody TripFlight flight) {
        return ResponseEntity.ok(tripPersistenceService.upsertFlight(tripId, flight));
    }

    @DeleteMapping("/{tripId}/flight")
    public ResponseEntity<Void> deleteFlight(@org.springframework.web.bind.annotation.PathVariable Integer tripId) {
        tripPersistenceService.deleteFlight(tripId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tripId}/hotel")
    public ResponseEntity<TripHotel> upsertHotel(@org.springframework.web.bind.annotation.PathVariable Integer tripId,
                                                 @RequestBody TripHotel hotel) {
        return ResponseEntity.ok(tripPersistenceService.upsertHotel(tripId, hotel));
    }

    @DeleteMapping("/{tripId}/hotel")
    public ResponseEntity<Void> deleteHotel(@org.springframework.web.bind.annotation.PathVariable Integer tripId) {
        tripPersistenceService.deleteHotel(tripId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tripId}/attractions")
    public ResponseEntity<TripAttraction> addAttraction(@org.springframework.web.bind.annotation.PathVariable Integer tripId,
                                                        @RequestBody TripAttraction attraction) {
        return ResponseEntity.ok(tripPersistenceService.addAttraction(tripId, attraction));
    }

    @PatchMapping("/attractions/{id}")
    public ResponseEntity<TripAttraction> updateAttraction(@org.springframework.web.bind.annotation.PathVariable Integer id,
                                                           @RequestBody TripAttraction attraction) {
        attraction.setTripAttractionId(id);
        return ResponseEntity.ok(tripPersistenceService.updateAttraction(attraction));
    }

    @DeleteMapping("/attractions/{id}")
    public ResponseEntity<Void> deleteAttraction(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        tripPersistenceService.deleteAttraction(id);
        return ResponseEntity.ok().build();
    }
}



