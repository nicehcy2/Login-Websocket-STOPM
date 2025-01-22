package hello.chat.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisMessage {
    private Long messageId;
    private Long senderId;
    private String content;
    private Long timestamp;
    private Long chatRoomId;

    // getter, setter 생략
}
