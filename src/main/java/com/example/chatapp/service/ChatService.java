package com.example.chatapp.service;

import com.example.chatapp.dto.ChatMessageResponse;
import com.example.chatapp.entity.*;
import com.example.chatapp.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final UserRepository userRepo;
    private final ChatRoomRepository chatRoomRepo;
    private final MessageRepository messageRepo;
    private final TranslationService translationService;

    public ChatService(UserRepository u, ChatRoomRepository c, MessageRepository m, TranslationService t) {
        this.userRepo = u;
        this.chatRoomRepo = c;
        this.messageRepo = m;
        this.translationService = t;
    }

    /**
     * FIX: This now does exactly ONE translation call and ONE database save per message,
     * and always persists (the old "forReceiver" flag caused REST-submitted messages to
     * never be saved at all, and caused the WebSocket path to translate the same text twice).
     * It returns a single response containing both the original and translated text;
     * callers (REST controller / WebSocket controller) decide what to show to whom.
     */
    public ChatMessageResponse processMessage(Long senderId, Long receiverId, String text) {
        User sender = userRepo.findById(senderId).orElseThrow();
        User receiver = userRepo.findById(receiverId).orElseThrow();

        ChatRoom room = chatRoomRepo.findChatRoom(senderId, receiverId)
                .orElseGet(() -> chatRoomRepo.save(new ChatRoom(null, senderId, receiverId)));

        String translated = translationService.translate(text, sender.getLanguage(), receiver.getLanguage());

        Message message = new Message();
        message.setChatRoomId(room.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setOriginalMessage(text);
        message.setTranslatedMessage(translated);
        Message saved = messageRepo.save(message);

        return ChatMessageResponse.builder()
                .senderId(senderId)
                .content(text)
                .originalMessage(text)
                .translatedMessage(translated)
                .timestamp(saved.getTimestamp())
                .build();
    }
}
