package com.example.chatapp.controller;

import com.example.chatapp.entity.User;
import com.example.chatapp.entity.Message;
import com.example.chatapp.dto.ChatMessageRequest;
import com.example.chatapp.dto.ChatMessageResponse;
import com.example.chatapp.service.ChatService;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.repository.MessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final ChatService chatService;

    public ChatController(UserRepository userRepo, MessageRepository messageRepo, ChatService chatService) {
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.chatService = chatService;
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepo.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // FIX: now actually persists the message (old code passed forReceiver=true,
    // which skipped the save block inside ChatService entirely).
    @PostMapping("/messages")
    public ChatMessageResponse sendMessage(@RequestBody ChatMessageRequest request) {
        return chatService.processMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getMessage()
        );
    }

    // FIX: only return messages where viewerId is actually the sender or the receiver.
    // The old version returned ALL messages in the system to ANY viewer.
    @GetMapping("/messages/view/{viewerId}")
    public List<Map<String, Object>> getChatForUser(@PathVariable Long viewerId) {
        List<Message> allMessages = messageRepo.findAll();
        List<Map<String, Object>> customView = new ArrayList<>();

        for (Message msg : allMessages) {
            boolean isSender = msg.getSenderId() != null && msg.getSenderId().equals(viewerId);
            boolean isReceiver = msg.getReceiverId() != null && msg.getReceiverId().equals(viewerId);

            if (!isSender && !isReceiver) {
                continue; // viewer has nothing to do with this message
            }

            Map<String, Object> entry = new HashMap<>();
            entry.put("id", msg.getId());
            entry.put("senderId", msg.getSenderId());
            entry.put("receiverId", msg.getReceiverId());
            entry.put("timestamp", msg.getTimestamp());
            // FIX: always include BOTH texts. The frontend decides which is the
            // "main" text and which is the small translation note based on role.
            entry.put("originalMessage", msg.getOriginalMessage());
            entry.put("translatedMessage", msg.getTranslatedMessage());

            if (isSender) {
                entry.put("displayText", msg.getOriginalMessage());
                entry.put("role", "SENDER");
            } else {
                entry.put("displayText", msg.getTranslatedMessage());
                entry.put("role", "RECEIVER");
            }
            customView.add(entry);
        }
        return customView;
    }

    @GetMapping("/messages")
    public List<Message> getAllMessages() {
        return messageRepo.findAll();
    }
}
