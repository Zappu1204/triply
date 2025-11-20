package com.triply.tripapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triply.tripapp.entity.ChatMessage;
import com.triply.tripapp.entity.ChatThread;
import com.triply.tripapp.integration.PerplexityClient;
import com.triply.tripapp.integration.PerplexityClient.Message;
import com.triply.tripapp.repository.ChatMessageRepository;
import com.triply.tripapp.repository.ChatThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PerplexityClient perplexityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Transactional
    public JsonNode sendMessage(Integer customerId, Integer threadId, String userMessage, String jsonSchema) throws IOException {
        ChatThread thread = chatThreadRepository.findById(threadId).orElseThrow(() -> new IllegalArgumentException("Thread not found"));
        if (!thread.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Thread does not belong to customer");
        }

        ChatMessage user = new ChatMessage();
        user.setThreadId(threadId);
        user.setRole("user");
        user.setContent(userMessage);
        chatMessageRepository.save(user);

        List<ChatMessage> history = chatMessageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        List<Message> msgs = new ArrayList<>();
        for (ChatMessage m : history) {
            msgs.add(new Message(m.getRole(), m.getContent()));
        }

        String body = perplexityClient.chatWithHistory(msgs, jsonSchema);
        JsonNode root = objectMapper.readTree(body);
        String content = root.path("choices").path(0).path("message").path("content").asText("");

        ChatMessage assistant = new ChatMessage();
        assistant.setThreadId(threadId);
        assistant.setRole("assistant");
        assistant.setContent(content);
        chatMessageRepository.save(assistant);

        thread.setLastMessageAt(LocalDateTime.now());
        chatThreadRepository.save(thread);

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



