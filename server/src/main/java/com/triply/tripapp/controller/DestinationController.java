package com.triply.tripapp.controller;

import com.triply.tripapp.entity.City;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.repository.CityRepository;
import com.triply.tripapp.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/destinations")
public class DestinationController {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DestinationRepository destinationRepository;

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
    public ResponseEntity<List<Destination>> getDestinationsByCity(@PathVariable Integer cityId) {
        return ResponseEntity.ok(destinationRepository.findByCityId(cityId));
    }

    @GetMapping
    public ResponseEntity<List<Destination>> getAllDestinations() {
        return ResponseEntity.ok(destinationRepository.findAll());
    }

    @GetMapping("/{destinationId}")
    public ResponseEntity<Destination> getDestinationById(@PathVariable Integer destinationId) {
        return destinationRepository.findById(destinationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

