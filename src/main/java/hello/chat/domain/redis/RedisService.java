package hello.chat.domain.redis;

import hello.chat.domain.chat.dto.STOMPChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 메시지 저장
    public void saveMessageToRedis(RedisMessage redisMessage) {

        String redisKey = "chat:room:" + redisMessage.getChatRoomId();
        redisTemplate.opsForList().leftPush(redisKey, redisMessage);
    }

    // Redis에서 메시지 조회
    public List<Object> getMessagesFromRedis(Long roomId) {

        String redisKey = "chat:room:" + roomId;
        return redisTemplate.opsForList().range(redisKey, 0, -1);// Redis List에서 메시지 조회
    }
}
