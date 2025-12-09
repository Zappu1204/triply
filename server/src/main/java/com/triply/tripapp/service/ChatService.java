package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triply.tripapp.entity.ChatMessage;
import com.triply.tripapp.entity.ChatThread;
import com.triply.tripapp.integration.PerplexityClient;
import com.triply.tripapp.integration.PerplexityClient.Message;
import com.triply.tripapp.repository.ChatMessageRepository;
import com.triply.tripapp.repository.ChatThreadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat Service với RAG (Retrieval-Augmented Generation)
 * 
 * Tự động retrieve context từ ChromaDB khi câu hỏi liên quan đến địa điểm du lịch
 */
@Service
@Slf4j
public class ChatService {

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PerplexityClient perplexityClient;

    @Autowired
    private RAGService ragService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // System prompt với hướng dẫn sử dụng context từ RAG
    private static final String RAG_SYSTEM_PROMPT = 
        "Bạn là trợ lý du lịch thông minh của Triply.\n" +
        "Nhiệm vụ: Tư vấn và gợi ý địa điểm du lịch tại Việt Nam.\n\n" +
        "QUY TẮC:\n" +
        "1. Nếu có THÔNG TIN TỪ CƠ SỞ DỮ LIỆU, ưu tiên sử dụng thông tin này\n" +
        "2. Trích dẫn tên địa điểm, rating, địa chỉ cụ thể từ database\n" +
        "3. Nếu không có thông tin trong database, dùng kiến thức của bạn\n" +
        "4. Luôn trả lời bằng tiếng Việt, thân thiện và hữu ích\n" +
        "5. Đề xuất 2-3 địa điểm cụ thể khi được hỏi\n" +
        "6. Kèm theo rating nếu có\n\n" +
        "Hãy trả lời câu hỏi của người dùng:";

    @Transactional
    public ChatThread startThread(Integer customerId, String title, String systemPrompt) {
        ChatThread thread = new ChatThread();
        thread.setCustomerId(customerId);
        thread.setTitle(title);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setLastMessageAt(LocalDateTime.now());
        ChatThread saved = chatThreadRepository.save(thread);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            ChatMessage sys = new ChatMessage();
            sys.setThreadId(saved.getThreadId());
            sys.setRole("system");
            sys.setContent(systemPrompt);
            chatMessageRepository.save(sys);
        }
        return saved;
    }

    /**
     * Send message với RAG support
     * Tự động retrieve context từ ChromaDB nếu câu hỏi liên quan đến địa điểm
     */
    @Transactional
    public JsonNode sendMessage(Integer customerId, Integer threadId, String userMessage, String jsonSchema) throws IOException {
        ChatThread thread = chatThreadRepository.findById(threadId)
            .orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        
        if (!thread.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Thread does not belong to customer");
        }

        // Lưu user message
        ChatMessage user = new ChatMessage();
        user.setThreadId(threadId);
        user.setRole("user");
        user.setContent(userMessage);
        chatMessageRepository.save(user);

        // RAG: Kiểm tra xem có cần retrieve context không
        String enhancedMessage = userMessage;
        boolean usedRAG = false;
        
        if (ragService.shouldUseRAG(userMessage)) {
            log.info("🔍 Câu hỏi liên quan đến địa điểm - Sử dụng RAG");
            
            // Extract keywords và search
            String keywords = ragService.extractKeywords(userMessage);
            String context = ragService.retrieveContext(keywords, 3);  // Top 3 results
            
            if (context != null && !context.isEmpty()) {
                // Enhance message với context từ database
                enhancedMessage = context + "\n\nCÂU HỎI: " + userMessage;
                usedRAG = true;
                log.info("✓ Đã thêm context từ ChromaDB vào prompt");
            } else {
                log.info("⚠ Không tìm thấy context phù hợp trong ChromaDB");
            }
        } else {
            log.info("💬 Câu hỏi thông thường - Không dùng RAG");
        }

        // Load chat history
        List<ChatMessage> history = chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        List<Message> msgs = new ArrayList<>();
        
        // Thêm system prompt nếu chưa có
        boolean hasSystemPrompt = history.stream().anyMatch(m -> "system".equals(m.getRole()));
        if (!hasSystemPrompt) {
            msgs.add(new Message("system", RAG_SYSTEM_PROMPT));
        }
        
        // Thêm chat history (trừ message cuối - đã có trong enhancedMessage)
        for (int i = 0; i < history.size() - 1; i++) {
            ChatMessage m = history.get(i);
            if (!"system".equals(m.getRole())) {  // Skip system prompt cũ nếu có
                msgs.add(new Message(m.getRole(), m.getContent()));
            }
        }
        
        // Thêm user message (có thể đã được enhance với RAG context)
        msgs.add(new Message("user", enhancedMessage));

        // Gọi Perplexity AI
        log.debug("Gọi Perplexity với {} messages", msgs.size());
        String body = perplexityClient.chatWithHistory(msgs, jsonSchema);
        JsonNode root = objectMapper.readTree(body);
        String content = root.path("choices").path(0).path("message").path("content").asText("");

        // Lưu assistant response
        ChatMessage assistant = new ChatMessage();
        assistant.setThreadId(threadId);
        assistant.setRole("assistant");
        assistant.setContent(content);
        chatMessageRepository.save(assistant);

        // Update thread timestamp
        thread.setLastMessageAt(LocalDateTime.now());
        chatThreadRepository.save(thread);

        // Add metadata về RAG usage vào response
        if (root.isObject()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("usedRAG", usedRAG);
        }

        return root;
    }

    public List<ChatThread> listThreads(Integer customerId) {
        return chatThreadRepository.findByCustomerIdOrderByLastMessageAtDesc(customerId);
    }

    public List<ChatMessage> listMessages(Integer customerId, Integer threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId).orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        if (!thread.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Thread does not belong to customer");
        }
        return chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
    }
}



