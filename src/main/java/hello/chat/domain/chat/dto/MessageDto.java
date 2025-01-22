package hello.chat.domain.chat.dto;

import java.time.LocalDateTime;

public record MessageDto(
        Long id,
        String messageType,
        String content,
        Long chatRoomId, // 그룹 채팅 방 번호 -> 클라이언트에서 보내줘야 될듯?
        Long senderId,
        LocalDateTime timestamp
) {
    /*
    public static MessageDto of(Message message) {

        return MessageDto.builder()
                .messageType(message.getMessageType().toString())
                .content(message.getContent())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getUser().getId())
                .build();
    }
     */
}
