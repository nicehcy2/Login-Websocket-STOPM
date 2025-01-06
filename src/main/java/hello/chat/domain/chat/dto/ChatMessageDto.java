package hello.chat.domain.chat.dto;

import hello.chat.domain.chat.entity.MessageType;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatMessageDto {

    private MessageType messageType; // 메시지 타입
    private Long chatRoomId; // 방 번호
    private Long senderId; // 채팅을 보낸 사람
    private String message; // 메시지
}
