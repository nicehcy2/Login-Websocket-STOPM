package hello.chat.domain.chat.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageDto(
        String id,
        Long chatRoomId, // 목적지(전달할 그룹 채팅방) ID
        Long senderId, // 발신인 ID
        String messageType, // 메시지 타입(텍스트, 사진, 영수증)
        String content, // 메시지 내용
        LocalDateTime timestamp, // 타임스탬프
        Integer unreadCount, // 읽지 않은 사용자
        Boolean rabbitMQTransmissionStatus, // RabbitMQ로 메시지 전송이 되었는지
        Boolean finalTransmissionStatus, // 최종적으로 메시지 전송이 되었는지
        Boolean saveStatus // Redis 저장 여부
) {
}