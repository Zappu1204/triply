package com.triply.tripapp.service;

import com.triply.tripapp.dto.EmbeddingResultDto;
import com.triply.tripapp.entity.City;
import com.triply.tripapp.entity.Destination;
import com.triply.tripapp.repository.CityRepository;
import com.triply.tripapp.repository.DestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DestinationEmbeddingService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private VectorStore vectorStore;

    /**
     * Embed tất cả destinations vào ChromaDB
     * @return EmbeddingResultDto với thống kê
     */
    @Transactional(readOnly = true)
    public EmbeddingResultDto embedAllDestinations() {
        log.info("Bắt đầu embedding tất cả destinations vào ChromaDB");

        List<Destination> destinations = destinationRepository.findAll();
        int totalDestinations = destinations.size();
        int successfullyEmbedded = 0;
        int failedEmbedding = 0;
        int skippedNoDescription = 0;

        List<Document> documents = new ArrayList<>();

        for (Destination destination : destinations) {
            try {
                // Skip nếu không có description
                if (destination.getDescription() == null || destination.getDescription().trim().isEmpty()) {
                    skippedNoDescription++;
                    log.debug("Bỏ qua destination '{}' - không có description", destination.getName());
                    continue;
                }

                // Tạo document để embed
                Document document = createDocumentFromDestination(destination);
                documents.add(document);
                
            } catch (Exception e) {
                log.error("Lỗi khi chuẩn bị document cho destination '{}': {}", 
                    destination.getName(), e.getMessage());
                failedEmbedding++;
            }
        }

        // Batch embed tất cả documents vào ChromaDB
        if (!documents.isEmpty()) {
            try {
                log.info("Đang embed {} documents vào ChromaDB...", documents.size());
                vectorStore.add(documents);
                successfullyEmbedded = documents.size();
                log.info("✓ Đã embed thành công {} destinations vào ChromaDB", successfullyEmbedded);
            } catch (Exception e) {
                log.error("Lỗi khi embed batch vào ChromaDB: {}", e.getMessage(), e);
                failedEmbedding = documents.size();
                successfullyEmbedded = 0;
            }
        }

        String message = String.format(
            "Embedding hoàn tất! Tổng: %d destinations. " +
            "Thành công: %d, Thất bại: %d, Bỏ qua (không có mô tả): %d",
            totalDestinations, successfullyEmbedded, failedEmbedding, skippedNoDescription
        );

        log.info(message);

        return new EmbeddingResultDto(
            totalDestinations,
            successfullyEmbedded,
            failedEmbedding,
            skippedNoDescription,
            message
        );
    }

    /**
     * Embed destinations của một city cụ thể
     * @param cityId City ID
     * @return EmbeddingResultDto với thống kê
     */
    @Transactional(readOnly = true)
    public EmbeddingResultDto embedDestinationsByCity(Integer cityId) {
        log.info("Bắt đầu embedding destinations cho city ID: {}", cityId);

        City city = cityRepository.findById(cityId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thành phố với ID: " + cityId));

        List<Destination> destinations = destinationRepository.findByCityId(cityId);
        int totalDestinations = destinations.size();
        int successfullyEmbedded = 0;
        int failedEmbedding = 0;
        int skippedNoDescription = 0;

        List<Document> documents = new ArrayList<>();

        for (Destination destination : destinations) {
            try {
                // Skip nếu không có description
                if (destination.getDescription() == null || destination.getDescription().trim().isEmpty()) {
                    skippedNoDescription++;
                    log.debug("Bỏ qua destination '{}' - không có description", destination.getName());
                    continue;
                }

                Document document = createDocumentFromDestination(destination);
                documents.add(document);
                
            } catch (Exception e) {
                log.error("Lỗi khi chuẩn bị document cho destination '{}': {}", 
                    destination.getName(), e.getMessage());
                failedEmbedding++;
            }
        }

        // Batch embed vào ChromaDB
        if (!documents.isEmpty()) {
            try {
                log.info("Đang embed {} documents cho city '{}' vào ChromaDB...", 
                    documents.size(), city.getName());
                vectorStore.add(documents);
                successfullyEmbedded = documents.size();
                log.info("✓ Đã embed thành công {} destinations của {} vào ChromaDB", 
                    successfullyEmbedded, city.getName());
            } catch (Exception e) {
                log.error("Lỗi khi embed batch vào ChromaDB: {}", e.getMessage(), e);
                failedEmbedding = documents.size();
                successfullyEmbedded = 0;
            }
        }

        String message = String.format(
            "Embedding hoàn tất cho %s! Tổng: %d destinations. " +
            "Thành công: %d, Thất bại: %d, Bỏ qua: %d",
            city.getName(), totalDestinations, successfullyEmbedded, failedEmbedding, skippedNoDescription
        );

        log.info(message);

        return new EmbeddingResultDto(
            totalDestinations,
            successfullyEmbedded,
            failedEmbedding,
            skippedNoDescription,
            message
        );
    }

    /**
     * Tạo Document từ Destination entity
     * Content: name + address + description
     * Metadata: destinationId, placeId, cityId, cityName, rating, types, openState
     * 
     * @param destination Destination entity
     * @return Document để embed
     */
    private Document createDocumentFromDestination(Destination destination) {
        // Tạo content: kết hợp name, address, description
        StringBuilder contentBuilder = new StringBuilder();
        
        if (destination.getName() != null) {
            contentBuilder.append("Tên: ").append(destination.getName()).append("\n");
        }
        
        if (destination.getAddress() != null && !destination.getAddress().isEmpty()) {
            contentBuilder.append("Địa chỉ: ").append(destination.getAddress()).append("\n");
        }
        
        if (destination.getDescription() != null && !destination.getDescription().isEmpty()) {
            contentBuilder.append("Mô tả: ").append(destination.getDescription());
        }

        String content = contentBuilder.toString().trim();

        // Tạo metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("destinationId", destination.getDestinationId());
        
        if (destination.getPlaceId() != null) {
            metadata.put("placeId", destination.getPlaceId());
        }
        
        if (destination.getCityId() != null) {
            metadata.put("cityId", destination.getCityId());
            
            // Lấy tên city
            cityRepository.findById(destination.getCityId()).ifPresent(city -> 
                metadata.put("cityName", city.getName())
            );
        }
        
        if (destination.getRating() != null) {
            metadata.put("rating", destination.getRating());
        }
        
        if (destination.getTypes() != null && !destination.getTypes().isEmpty()) {
            metadata.put("types", destination.getTypes());
        }
        
        if (destination.getOpenState() != null && !destination.getOpenState().isEmpty()) {
            metadata.put("openState", destination.getOpenState());
        }

        if (destination.getWebsite() != null && !destination.getWebsite().isEmpty()) {
            metadata.put("website", destination.getWebsite());
        }

        if (destination.getGoogleMapUrl() != null && !destination.getGoogleMapUrl().isEmpty()) {
            metadata.put("googleMapUrl", destination.getGoogleMapUrl());
        }

        // Tạo unique ID cho document
        String documentId = "dest_" + destination.getDestinationId();

        return new Document(documentId, content, metadata);
    }

    /**
     * Xóa tất cả embeddings trong ChromaDB
     * Cẩn thận: Thao tác này không thể hoàn tác!
     * 
     * @return Message xác nhận
     */
    public String clearAllEmbeddings() {
        try {
            log.warn("Đang xóa tất cả embeddings trong ChromaDB...");
            
            // ChromaDB VectorStore không có method clear trực tiếp
            // Cần implement thủ công hoặc recreate collection
            // Tạm thời throw exception để user biết
            throw new UnsupportedOperationException(
                "Chức năng xóa toàn bộ embeddings chưa được implement. " +
                "Vui lòng xóa collection 'attractions' trực tiếp trong ChromaDB."
            );
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa embeddings: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi xóa embeddings: " + e.getMessage(), e);
        }
    }
}

