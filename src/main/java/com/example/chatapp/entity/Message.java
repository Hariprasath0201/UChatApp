package com.example.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatRoomId;
    private Long senderId;

    // FIX: needed so we can tell who a message was meant for.
    // Without this, /messages/view/{viewerId} cannot correctly filter conversations.
    private Long receiverId;

    @Column(length = 2000)
    private String originalMessage;

    @Column(length = 2000)
    private String translatedMessage;

    private LocalDateTime timestamp;

    // FIX: only set the timestamp if it hasn't already been set,
    // otherwise this silently overwrites any timestamp passed in from the service layer.
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
