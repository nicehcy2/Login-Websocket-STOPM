package hello.chat.domain.chat.entity;

import jakarta.persistence.*;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType messageType; // 메시지 타입
    private Long chatRoomId; // 방 번호
    private Long senderId; // 채팅을 보낸 사람
    private String message; // 메시지
}
