package com.triply.tripapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.triply.tripapp.dto.response.DestinationWithLocationResponse;
import com.triply.tripapp.entity.City;
import com.triply.tripapp.repository.CityRepository;
import com.triply.tripapp.service.DestinationService;

@RestController
@RequestMapping("/destinations")
public class DestinationController {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DestinationService destinationService;

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityRepository.findAll());
    }

    @GetMapping("/cities/{cityId}")
    public ResponseEntity<City> getCityById(@PathVariable Integer cityId) {
        return cityRepository.findById(cityId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cities/{cityId}/destinations")
    public ResponseEntity<List<DestinationWithLocationResponse>> getDestinationsByCity(@PathVariable Integer cityId) {
        return ResponseEntity.ok(destinationService.getDestinationsByCity(cityId));
    }

    @GetMapping
    public ResponseEntity<List<DestinationWithLocationResponse>> getAllDestinations() {
        return ResponseEntity.ok(destinationService.getAllDestinations());
    }

    @GetMapping("/{destinationId}")
    public ResponseEntity<DestinationWithLocationResponse> getDestinationById(@PathVariable Integer destinationId) {
        return destinationService.getDestinationById(destinationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

