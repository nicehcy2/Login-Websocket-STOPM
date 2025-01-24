package hello.chat.domain.chat.service;

import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.entity.ChatRoom;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate; // redis
    private final RabbitTemplate rabbitTemplate; // rabbitmq

    @Value("${rabbitmq.exchange.name}")
    private String CHAT_EXCHANGE_NAME;
    private static final String READ_STATUS_KEY_PREFIX = "read:message:";

    public List<MessageDto> findMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messgeList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messgeList.stream()
                .map(object -> (MessageDto) object)
                .toList();
    }

    @Transactional
    public void saveMessages(MessageDto messageDto) {

        User sender = userRepository.findById(messageDto.senderId() )
                .orElseThrow(() -> new RuntimeException());

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.chatRoomId())
                .orElseThrow(() -> new RuntimeException());

        String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";
        redisTemplate.opsForList().leftPush(redisKey, messageDto);
    }

    // RabbitMQ 브로커를 사용해서 특정 그룹 채팅방에 메시지를 보낸다.
    public void sendMessage(MessageDto messageDto) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto);
    }

    // 메시지 읽음 처리
    public void markMessageAsRead(Long chatRoomId, Long messageId, Long userId) {
        String key = READ_STATUS_KEY_PREFIX + chatRoomId.toString() + ":" + messageId.toString();
        redisTemplate.opsForSet().add(key, userId);
    }

    // 메시지 읽음 수 조회
    public long getReadCount(Long chatRoomId, Long messageId) {
        String key = READ_STATUS_KEY_PREFIX + chatRoomId.toString() + ":" + messageId.toString();
        return redisTemplate.opsForSet().size(key);
    }
}