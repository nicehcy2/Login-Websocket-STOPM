package hello.chat.domain.chat.entity;

import hello.chat.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "messageId", nullable = false, length = 19)
    private String messageId;

    @Column(name = "chatRoomId", nullable = false)
    private Long chatRoomId; // 목적지(전달할 그룹 채팅방) ID

    @Column(name = "senderId", nullable = false)
    private Long senderId; // 발신인 ID

    @Column(name = "messageType", nullable = false, length = 10)
    private String messageType; // 메시지 타입(텍스트, 사진, 영수증)

    private String content; // 메시지 내용
    private String timestamp; // 타임스탬프

    @Column(name = "unreadCount", nullable = false)
    private Integer unreadCount; // 읽지 않은 사용자

    private Integer publishRetryCount; // 메시지 전송 재시도 횟수
    private Boolean saveStatus; // Redis 저장 여부
}
