package com.triply.tripapp.controller;

import com.triply.tripapp.entity.Expense;
import com.triply.tripapp.entity.ItineraryItem;
import com.triply.tripapp.service.TripPlanningService.PlanRequest;
import com.triply.tripapp.service.TripPlanningService.PlanResponse;
import com.triply.tripapp.service.TripPlanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/plan")
    public ResponseEntity<PlanResponse> plan(@RequestBody PlanRequest request) throws Exception {
        return ResponseEntity.ok(tripPlanningService.plan(request));
    }

    @PostMapping("/save")
    public ResponseEntity<Integer> save(Authentication authentication, @RequestBody SaveRequest req) {
        // In a complete app, map authentication principal to customerId
        Integer customerId = 1; // TODO: replace with real mapping
        return ResponseEntity.ok(tripPlanningService
                .savePlannedTrip(customerId, req.title, req.startDate, req.endDate, req.totalBudget, req.currency, req.items, req.estimatedExpenses)
                .getTripId());
    }
}



