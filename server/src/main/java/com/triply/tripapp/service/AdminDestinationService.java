package com.triply.tripapp.service;

import com.triply.tripapp.entity.City;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.entity.Region;
import com.triply.tripapp.repository.CityRepository;
import com.triply.tripapp.repository.DestinationRepository;
import com.triply.tripapp.repository.RegionRepository;
import com.triply.tripapp.util.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDestinationService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    // ========== Region Management ==========
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }

    public Region getRegionById(Integer id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Region not found"));
    }

    @Transactional
    public Region createRegion(Region region) {
        region.setCreatedAt(LocalDateTime.now());
        return regionRepository.save(region);
    }

    @Transactional
    public Region updateRegion(Integer id, Region region) {
        Region existing = getRegionById(id);
        existing.setName(region.getName());
        existing.setDescription(region.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        return regionRepository.save(existing);
    }

    @Transactional
    public void deleteRegion(Integer id) {
        regionRepository.deleteById(id);
    }

    // ========== City Management ==========
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public List<City> getCitiesByRegion(Integer regionId) {
        return cityRepository.findByRegionId(regionId);
    }

    public City getCityById(Integer id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("City not found"));
    }

    @Transactional
    public City createCity(City city) {
        city.setCreatedAt(LocalDateTime.now());
        return cityRepository.save(city);
    }

    @Transactional
    public City updateCity(Integer id, City city) {
        City existing = getCityById(id);
        existing.setName(city.getName());
        existing.setRegionId(city.getRegionId());
        existing.setLatitude(city.getLatitude());
        existing.setLongitude(city.getLongitude());
        existing.setDescription(city.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        return cityRepository.save(existing);
    }

    @Transactional
    public void deleteCity(Integer id) {
        cityRepository.deleteById(id);
    }

    // ========== Destination Management ==========
    public List<Destination> getAllDestinations() {
        return destinationRepository.findAll();
    }

    public List<Destination> getDestinationsByCity(Integer cityId) {
        return destinationRepository.findByCityId(cityId);
    }

    public Destination getDestinationById(Integer id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Destination not found"));
    }

    @Transactional
    public Destination createDestination(Destination destination) {
        destination.setCreatedAt(LocalDateTime.now());
        return destinationRepository.save(destination);
    }

    @Transactional
    public Destination updateDestination(Integer id, Destination destination) {
        Destination existing = getDestinationById(id);
        existing.setName(destination.getName());
        existing.setAddress(destination.getAddress());
        existing.setImgPath(destination.getImgPath());
        existing.setGoogleMapUrl(destination.getGoogleMapUrl());
        existing.setDescription(destination.getDescription());
        existing.setCityId(destination.getCityId());
        existing.setUpdatedAt(LocalDateTime.now());
        return destinationRepository.save(existing);
    }

    @Transactional
    public void deleteDestination(Integer id) {
        destinationRepository.deleteById(id);
    }
}

