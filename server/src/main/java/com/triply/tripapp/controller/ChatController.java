package com.triply.tripapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.triply.tripapp.entity.ChatMessage;
import com.triply.tripapp.entity.ChatThread;
import com.triply.tripapp.service.ChatService;
import com.triply.tripapp.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    public static class StartThreadRequest {
        public String title;
        public String systemPrompt;
    }

    public static class SendMessageRequest {
        public Integer threadId;
        public String message;
        public String jsonSchema; // optional
    }

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/threads")
    public ResponseEntity<ChatThread> start(Authentication authentication, @RequestBody StartThreadRequest req) {
        Integer customerId = authUtil.getCustomerId(authentication);
        return ResponseEntity.ok(chatService.startThread(customerId, req.title, req.systemPrompt));
    }

    @GetMapping("/threads")
    public ResponseEntity<List<ChatThread>> listThreads(Authentication authentication) {
        Integer customerId = authUtil.getCustomerId(authentication);
        return ResponseEntity.ok(chatService.listThreads(customerId));
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<List<ChatMessage>> listMessages(Authentication authentication, @PathVariable Integer threadId) {
        Integer customerId = authUtil.getCustomerId(authentication);
        return ResponseEntity.ok(chatService.listMessages(customerId, threadId));
    }

    @PostMapping("/send")
    public ResponseEntity<JsonNode> send(Authentication authentication, @RequestBody SendMessageRequest req) throws IOException {
        Integer customerId = authUtil.getCustomerId(authentication);
        return ResponseEntity.ok(chatService.sendMessage(customerId, req.threadId, req.message, req.jsonSchema));
    }
}



