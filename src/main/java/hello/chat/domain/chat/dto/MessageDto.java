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
        String timestamp, // 타임스탬프
        Integer unreadCount, // 읽지 않은 사용자
        Integer publishRetryCount, // 메시지 전송 재시도 횟수
        Boolean saveStatus // Redis 저장 여부
) {
}