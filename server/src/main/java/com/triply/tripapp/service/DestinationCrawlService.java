package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triply.tripapp.dto.CrawlResultDto;
import com.triply.tripapp.dto.serpapi.GoogleMapsResponse;
import com.triply.tripapp.dto.serpapi.LocalResult;
import com.triply.tripapp.entity.City;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.entity.Location;
import com.triply.tripapp.integration.SerpApiClient;
import com.triply.tripapp.repository.CityRepository;
import com.triply.tripapp.repository.DestinationRepository;
import com.triply.tripapp.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DestinationCrawlService {

    @Autowired
    private SerpApiClient serpApiClient;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Crawl tourist attractions and historical sites for all cities
     * @return CrawlResultDto with statistics
     */
    @Transactional
    public CrawlResultDto crawlAllCities() {
        log.info("Bắt đầu crawl dữ liệu địa điểm du lịch cho tất cả các thành phố");
        
        List<City> cities = cityRepository.findAll();
        int totalCitiesProcessed = 0;
        int totalDestinationsFound = 0;
        int newDestinationsAdded = 0;
        int existingDestinationsSkipped = 0;

        for (City city : cities) {
            try {
                log.info("Đang crawl dữ liệu cho thành phố: {}", city.getName());
                
                CrawlResultDto cityResult = crawlCityDestinations(city);
                
                totalCitiesProcessed++;
                totalDestinationsFound += cityResult.getTotalDestinationsFound();
                newDestinationsAdded += cityResult.getNewDestinationsAdded();
                existingDestinationsSkipped += cityResult.getExistingDestinationsSkipped();
                
                log.info("Hoàn thành crawl cho {}: {} địa điểm mới, {} địa điểm đã tồn tại", 
                    city.getName(), 
                    cityResult.getNewDestinationsAdded(), 
                    cityResult.getExistingDestinationsSkipped());
                
                // Add delay to avoid rate limiting
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("Lỗi khi crawl dữ liệu cho thành phố {}: {}", city.getName(), e.getMessage(), e);
            }
        }

        String message = String.format(
            "Crawl hoàn tất! Đã xử lý %d/%d thành phố. " +
            "Tìm thấy %d địa điểm, thêm mới %d, bỏ qua %d địa điểm đã tồn tại.",
            totalCitiesProcessed, cities.size(),
            totalDestinationsFound, newDestinationsAdded, existingDestinationsSkipped
        );
        
        log.info(message);

        return new CrawlResultDto(
            totalCitiesProcessed,
            totalDestinationsFound,
            newDestinationsAdded,
            existingDestinationsSkipped,
            message
        );
    }

    /**
     * Crawl tourist attractions for a specific city
     * @param cityId City ID
     * @return CrawlResultDto with statistics
     */
    @Transactional
    public CrawlResultDto crawlCityById(Integer cityId) {
        log.info("Bắt đầu crawl dữ liệu cho thành phố ID: {}", cityId);
        
        City city = cityRepository.findById(cityId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thành phố với ID: " + cityId));
        
        return crawlCityDestinations(city);
    }

    /**
     * Internal method to crawl destinations for a city
     * @param city City entity
     * @return CrawlResultDto with statistics
     */
    private CrawlResultDto crawlCityDestinations(City city) {
        int destinationsFound = 0;
        int newDestinationsAdded = 0;
        int existingDestinationsSkipped = 0;

        try {
            // Check if city has coordinates
            if (city.getLatitude() == null || city.getLongitude() == null) {
                String message = String.format("Thành phố %s không có tọa độ GPS", city.getName());
                log.warn(message);
                return new CrawlResultDto(1, 0, 0, 0, message);
            }

            // Call SerpAPI to search for tourist attractions
            String jsonResponse = serpApiClient.searchGoogleMaps(
                "tourist attractions, historical sites",
                city.getLatitude().toString(),
                city.getLongitude().toString(),
                "13",
                "vi",
                "vn"
            );

            // Parse JSON response
            GoogleMapsResponse response = objectMapper.readValue(jsonResponse, GoogleMapsResponse.class);

            if (response.getLocalResults() != null && !response.getLocalResults().isEmpty()) {
                destinationsFound = response.getLocalResults().size();

                for (LocalResult result : response.getLocalResults()) {
                    // Check if destination already exists by placeId
                    if (result.getPlaceId() != null && 
                        destinationRepository.existsByPlaceId(result.getPlaceId())) {
                        existingDestinationsSkipped++;
                        log.debug("Địa điểm đã tồn tại: {}", result.getTitle());
                        continue;
                    }

                    // Save trong transaction riêng để tránh rollback toàn bộ
                    boolean saved = saveDestinationWithLocation(result, city);
                    if (saved) {
                        newDestinationsAdded++;
                    }
                }
            }

            String message = String.format(
                "Thành phố %s: Tìm thấy %d địa điểm, thêm mới %d, bỏ qua %d",
                city.getName(), destinationsFound, newDestinationsAdded, existingDestinationsSkipped
            );

            return new CrawlResultDto(
                1,
                destinationsFound,
                newDestinationsAdded,
                existingDestinationsSkipped,
                message
            );

        } catch (IOException e) {
            log.error("Lỗi khi gọi SerpAPI cho thành phố {}: {}", city.getName(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi crawl dữ liệu từ SerpAPI: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi xử lý thành phố {}: {}", city.getName(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xử lý dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Save destination và location trong transaction riêng
     * Mỗi destination là một transaction độc lập để tránh rollback toàn bộ
     * 
     * @param result LocalResult from SerpAPI
     * @param city City entity
     * @return true nếu save thành công, false nếu có lỗi
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveDestinationWithLocation(LocalResult result, City city) {
        try {
            // Create and save destination
            Destination destination = mapToDestination(result, city);
            destination = destinationRepository.saveAndFlush(destination);
            
            log.debug("Đã lưu destination: {} (ID: {})", destination.getName(), destination.getDestinationId());
            
            // Create and save location if GPS coordinates are available
            if (result.getGpsCoordinates() != null && 
                result.getGpsCoordinates().getLatitude() != null && 
                result.getGpsCoordinates().getLongitude() != null) {
                
                // Check if location already exists
                if (!locationRepository.existsById(destination.getDestinationId())) {
                    Location location = new Location();
                    // QUAN TRỌNG: Chỉ set destination, @MapsId sẽ tự động lấy ID
                    location.setDestination(destination);
                    location.setLatitude(result.getGpsCoordinates().getLatitude());
                    location.setLongitude(result.getGpsCoordinates().getLongitude());
                    location.setDescription(result.getType());
                    
                    locationRepository.saveAndFlush(location);
                    log.debug("Đã lưu location cho destination ID: {}", destination.getDestinationId());
                }
            }
            
            log.info("✓ Đã thêm địa điểm: {}", result.getTitle());
            return true;
            
        } catch (Exception e) {
            log.error("✗ Lỗi khi lưu destination '{}': {} - {}", 
                result.getTitle(), e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * Map LocalResult from SerpAPI to Destination entity
     * @param result LocalResult from SerpAPI
     * @param city City entity
     * @return Destination entity
     */
    private Destination mapToDestination(LocalResult result, City city) {
        Destination destination = new Destination();
        
        destination.setName(result.getTitle());
        destination.setAddress(result.getAddress());
        destination.setDescription(result.getDescription());
        destination.setPlaceId(result.getPlaceId());
        destination.setRating(result.getRating());
        destination.setReviewCount(result.getReviews());
        destination.setWebsite(result.getWebsite());
        destination.setOpenState(result.getOpenState());
        destination.setCityId(city.getCityId());
        
        // Set image path (use thumbnail from SerpAPI)
        if (result.getThumbnail() != null && !result.getThumbnail().isEmpty()) {
            destination.setImgPath(result.getThumbnail());
        } else if (result.getSerpapiThumbnail() != null && !result.getSerpapiThumbnail().isEmpty()) {
            destination.setImgPath(result.getSerpapiThumbnail());
        }
        
        // Set Google Maps URL
        if (result.getPlaceIdSearch() != null) {
            destination.setGoogleMapUrl(result.getPlaceIdSearch());
        }
        
        // Join type IDs as comma-separated string
        if (result.getTypeIds() != null && !result.getTypeIds().isEmpty()) {
            destination.setTypes(String.join(",", result.getTypeIds()));
        }
        
        destination.setCreatedAt(LocalDateTime.now());
        
        return destination;
    }
}

