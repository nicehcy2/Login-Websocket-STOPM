package hello.chat.domain.chat.dto;

import hello.chat.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class STOMPChatMessageDto {

    private String messageType;
    private String content;
    private Long chatRoomId; // 그룹 채팅 방 번호 -> 클라이언트에서 보내줘야 될듯?
    private Long senderId;

    public static STOMPChatMessageDto of(ChatMessage chatMessage) {

        return STOMPChatMessageDto.builder()
                .messageType(chatMessage.getMessageType().toString())
                .content(chatMessage.getContent())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(chatMessage.getUser().getId())
                .build();
    }
}
