package com.triply.tripapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.triply.tripapp.dto.response.DestinationWithLocationResponse;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.entity.Location;
import com.triply.tripapp.repository.DestinationRepository;
import com.triply.tripapp.repository.LocationRepository;

@Service
public class DestinationService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private LocationRepository locationRepository;

    public List<DestinationWithLocationResponse> getAllDestinations() {
        return destinationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<DestinationWithLocationResponse> getDestinationById(Integer destinationId) {
        return destinationRepository.findById(destinationId)
                .map(this::mapToResponse);
    }

    public List<DestinationWithLocationResponse> getDestinationsByCity(Integer cityId) {
        return destinationRepository.findByCityId(cityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DestinationWithLocationResponse mapToResponse(Destination destination) {
        DestinationWithLocationResponse response = new DestinationWithLocationResponse();
        
        response.setDestinationId(destination.getDestinationId());
        response.setName(destination.getName());
        response.setAddress(destination.getAddress());
        response.setImgPath(destination.getImgPath());
        response.setGoogleMapUrl(destination.getGoogleMapUrl());
        response.setDescription(destination.getDescription());
        response.setCityId(destination.getCityId());
        response.setPlaceId(destination.getPlaceId());
        response.setRating(destination.getRating());
        response.setReviewCount(destination.getReviewCount());
        response.setTypes(destination.getTypes());
        response.setWebsite(destination.getWebsite());
        response.setOpenState(destination.getOpenState());
        response.setCreatedAt(destination.getCreatedAt());
        response.setUpdatedAt(destination.getUpdatedAt());
        
        // Get location if exists
        Optional<Location> location = locationRepository.findById(destination.getDestinationId());
        if (location.isPresent()) {
            response.setLatitude(location.get().getLatitude());
            response.setLongitude(location.get().getLongitude());
        }
        
        return response;
    }
}
