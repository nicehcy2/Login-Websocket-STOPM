package hello.chat.domain.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redis")
public class RedisController {

    private final RedisService redisService;

    @PostMapping
    public void saveMessage() {

        Long roomId = 1L;  // 채팅방 ID
        RedisMessage message = new RedisMessage(1L, 45L, "Hello, World!", System.currentTimeMillis(), roomId);

        // 메시지를 Redis에 저장
        redisService.saveMessageToRedis(message);
    }

    @GetMapping
    public void getMessages() {

        Long roomId = 1L;  // 채팅방 ID

        // Redis에서 메시지 조회
        List<Object> messages = redisService.getMessagesFromRedis(roomId);
        System.out.println(messages.size());
        List<RedisMessage> messageList = new ArrayList<>();

        for (Object obj : messages) {
            // Object를 Message 타입으로 캐스팅
            if (obj instanceof RedisMessage) {
                messageList.add((RedisMessage) obj);
            }
        }
        messageList.forEach(msg -> System.out.println(msg.getContent()));
    }
}
