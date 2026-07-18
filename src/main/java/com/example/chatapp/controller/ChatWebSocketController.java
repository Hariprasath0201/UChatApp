package com.example.chatapp.controller;

import com.example.chatapp.dto.*;
import com.example.chatapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWebSocketController(SimpMessagingTemplate m, ChatService c) {
        this.messagingTemplate = m;
        this.chatService = c;
    }

    // FIX: was calling processMessage() twice (double translation API calls, double save
    // risk). Now calls it once and builds the two views (sender/receiver) from that one result.
    @MessageMapping("/chat.send")
    public void send(ChatMessageRequest request) {
        ChatMessageResponse result = chatService.processMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getMessage()
        );

        // Sender sees their own original text
        ChatMessageResponse senderView = ChatMessageResponse.builder()
                .senderId(result.getSenderId())
                .content(result.getOriginalMessage())
                .originalMessage(result.getOriginalMessage())
                .translatedMessage(null)
                .timestamp(result.getTimestamp())
                .build();

        // Receiver sees the translated text (plus original, if you want to show both)
        ChatMessageResponse receiverView = ChatMessageResponse.builder()
                .senderId(result.getSenderId())
                .content(result.getTranslatedMessage())
                .originalMessage(result.getOriginalMessage())
                .translatedMessage(result.getTranslatedMessage())
                .timestamp(result.getTimestamp())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + request.getSenderId(), senderView);
        messagingTemplate.convertAndSend("/topic/chat/" + request.getReceiverId(), receiverView);
    }
}
