package com.triply.tripapp.controller;

import com.triply.tripapp.dto.CrawlResultDto;
import com.triply.tripapp.dto.EmbeddingResultDto;
import com.triply.tripapp.entity.City;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.entity.Region;
import com.triply.tripapp.service.AdminDestinationService;
import com.triply.tripapp.service.DestinationCrawlService;
import com.triply.tripapp.service.DestinationEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminDestinationService adminDestinationService;

    @Autowired
    private DestinationCrawlService destinationCrawlService;

    @Autowired
    private DestinationEmbeddingService destinationEmbeddingService;

    // ========== Region Management ==========
    @GetMapping("/regions")
    public ResponseEntity<List<Region>> getAllRegions() {
        return ResponseEntity.ok(adminDestinationService.getAllRegions());
    }

    @GetMapping("/regions/{id}")
    public ResponseEntity<Region> getRegionById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminDestinationService.getRegionById(id));
    }

    @PostMapping("/regions")
    public ResponseEntity<Region> createRegion(@RequestBody Region region) {
        return ResponseEntity.ok(adminDestinationService.createRegion(region));
    }

    @PutMapping("/regions/{id}")
    public ResponseEntity<Region> updateRegion(@PathVariable Integer id, @RequestBody Region region) {
        return ResponseEntity.ok(adminDestinationService.updateRegion(id, region));
    }

    @DeleteMapping("/regions/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Integer id) {
        adminDestinationService.deleteRegion(id);
        return ResponseEntity.ok().build();
    }

    // ========== City Management ==========
    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(adminDestinationService.getAllCities());
    }

    @GetMapping("/regions/{regionId}/cities")
    public ResponseEntity<List<City>> getCitiesByRegion(@PathVariable Integer regionId) {
        return ResponseEntity.ok(adminDestinationService.getCitiesByRegion(regionId));
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminDestinationService.getCityById(id));
    }

    @PostMapping("/cities")
    public ResponseEntity<City> createCity(@RequestBody City city) {
        return ResponseEntity.ok(adminDestinationService.createCity(city));
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<City> updateCity(@PathVariable Integer id, @RequestBody City city) {
        return ResponseEntity.ok(adminDestinationService.updateCity(id, city));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
        adminDestinationService.deleteCity(id);
        return ResponseEntity.ok().build();
    }

    // ========== Destination Management ==========
    @GetMapping("/destinations")
    public ResponseEntity<List<Destination>> getAllDestinations() {
        return ResponseEntity.ok(adminDestinationService.getAllDestinations());
    }

    @GetMapping("/cities/{cityId}/destinations")
    public ResponseEntity<List<Destination>> getDestinationsByCity(@PathVariable Integer cityId) {
        return ResponseEntity.ok(adminDestinationService.getDestinationsByCity(cityId));
    }

    @GetMapping("/destinations/{id}")
    public ResponseEntity<Destination> getDestinationById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminDestinationService.getDestinationById(id));
    }

    @PostMapping("/destinations")
    public ResponseEntity<Destination> createDestination(@RequestBody Destination destination) {
        return ResponseEntity.ok(adminDestinationService.createDestination(destination));
    }

    @PutMapping("/destinations/{id}")
    public ResponseEntity<Destination> updateDestination(@PathVariable Integer id, @RequestBody Destination destination) {
        return ResponseEntity.ok(adminDestinationService.updateDestination(id, destination));
    }

    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<Void> deleteDestination(@PathVariable Integer id) {
        adminDestinationService.deleteDestination(id);
        return ResponseEntity.ok().build();
    }

    // ========== Destination Crawl Management ==========
    
    /**
     * Crawl tourist attractions and historical sites for all cities
     * This endpoint will call SerpAPI to get destination data for each city
     * and insert them into the database
     * 
     * @return CrawlResultDto with statistics about the crawl operation
     */
    @PostMapping("/destinations/crawl/all")
    public ResponseEntity<CrawlResultDto> crawlAllCities() {
        CrawlResultDto result = destinationCrawlService.crawlAllCities();
        return ResponseEntity.ok(result);
    }

    /**
     * Crawl tourist attractions and historical sites for a specific city
     * 
     * @param cityId ID of the city to crawl
     * @return CrawlResultDto with statistics about the crawl operation
     */
    @PostMapping("/destinations/crawl/city/{cityId}")
    public ResponseEntity<CrawlResultDto> crawlCityById(@PathVariable Integer cityId) {
        CrawlResultDto result = destinationCrawlService.crawlCityById(cityId);
        return ResponseEntity.ok(result);
    }

    // ========== Destination Embedding Management ==========
    
    /**
     * Embed tất cả destinations vào ChromaDB vector store
     * Sử dụng Azure OpenAI embedding model
     * Content: name + address + description
     * Metadata: cityId, cityName, rating, types, openState
     * 
     * @return EmbeddingResultDto with statistics about the embedding operation
     */
    @PostMapping("/destinations/embed/all")
    public ResponseEntity<EmbeddingResultDto> embedAllDestinations() {
        EmbeddingResultDto result = destinationEmbeddingService.embedAllDestinations();
        return ResponseEntity.ok(result);
    }

    /**
     * Embed destinations của một city cụ thể vào ChromaDB
     * 
     * @param cityId ID of the city
     * @return EmbeddingResultDto with statistics about the embedding operation
     */
    @PostMapping("/destinations/embed/city/{cityId}")
    public ResponseEntity<EmbeddingResultDto> embedDestinationsByCity(@PathVariable Integer cityId) {
        EmbeddingResultDto result = destinationEmbeddingService.embedDestinationsByCity(cityId);
        return ResponseEntity.ok(result);
    }
}

