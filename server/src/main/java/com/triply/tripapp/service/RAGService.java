package com.triply.tripapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Service
 * 
 * Sử dụng ChromaDB để retrieve relevant context cho chatbot
 */
@Service
@Slf4j
public class RAGService {

    @Autowired
    private VectorStore vectorStore;

    /**
     * Search trong ChromaDB để lấy relevant destinations
     * 
     * @param query User query/question
     * @param topK Number of results to return
     * @return Context string kết hợp từ các documents tìm được
     */
    public String retrieveContext(String query, int topK) {
        try {
            log.debug("Searching ChromaDB with query: {}", query);
            
            // Search trong vector store
            SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            if (results.isEmpty()) {
                log.debug("Không tìm thấy kết quả phù hợp trong ChromaDB");
                return null;
            }
            
            log.info("Tìm thấy {} destinations phù hợp", results.size());
            
            // Kết hợp contexts từ các documents
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("THÔNG TIN TỪ CƠ SỞ DỮ LIỆU:\n\n");
            
            int index = 1;
            for (Document doc : results) {
                String content = doc.getText();
                contextBuilder.append(String.format("%d. %s\n", index++, content));
                
                // Thêm metadata quan trọng
                if (doc.getMetadata().containsKey("rating")) {
                    contextBuilder.append(String.format("   Rating: %s/5\n", 
                        doc.getMetadata().get("rating")));
                }
                if (doc.getMetadata().containsKey("cityName")) {
                    contextBuilder.append(String.format("   Thành phố: %s\n", 
                        doc.getMetadata().get("cityName")));
                }
                contextBuilder.append("\n");
            }
            
            return contextBuilder.toString();
            
        } catch (Exception e) {
            log.error("Lỗi khi search trong ChromaDB: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Kiểm tra xem query có liên quan đến địa điểm du lịch không
     * Dùng keywords để quyết định có cần RAG hay không
     * 
     * @param query User query
     * @return true nếu cần dùng RAG
     */
    public boolean shouldUseRAG(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Keywords liên quan đến địa điểm du lịch
        String[] locationKeywords = {
            "địa điểm", "du lịch", "tham quan", "điểm đến",
            "đi đâu", "chơi gì", "thăm", "ghé",
            "hà nội", "huế", "hội an", "sài gòn", "tp.hcm",
            "lăng", "chùa", "đền", "bảo tàng", "di tích",
            "nơi", "chỗ", "recommend", "gợi ý", "đề xuất",
            "rating", "đánh giá", "nổi tiếng"
        };
        
        for (String keyword : locationKeywords) {
            if (lowerQuery.contains(keyword)) {
                log.debug("Query chứa keyword '{}' - Sẽ dùng RAG", keyword);
                return true;
            }
        }
        
        log.debug("Query không liên quan đến địa điểm - Không dùng RAG");
        return false;
    }

    /**
     * Extract keywords từ query để search tốt hơn
     * 
     * @param query User query
     * @return Cleaned query for better search
     */
    public String extractKeywords(String query) {
        // Remove common stop words và giữ lại keywords quan trọng
        String cleaned = query
            .toLowerCase()
            .replaceAll("\\b(cho tôi|tôi muốn|bạn có thể|hãy|xin)\\b", "")
            .replaceAll("\\s+", " ")
            .trim();
        
        log.debug("Original query: {} -> Extracted: {}", query, cleaned);
        return cleaned;
    }
}

