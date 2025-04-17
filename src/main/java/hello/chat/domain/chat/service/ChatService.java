package hello.chat.domain.chat.service;

import com.github.f4b6a3.tsid.TsidCreator;
import hello.chat.domain.chat.dto.MessageCorrelationData;
import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.entity.ChatRoom;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

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

        // TODO: userCount 저장하는 로직 추가

        MessageDto message = MessageDto.builder()
                .id(String.valueOf(TsidCreator.getTsid().toLong())) // TSID ID 생성기, 시간에 따라 ID에 영향이 가고 최신 데이터일수록 ID 값이 커진다.
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .messageType(messageDto.messageType())
                .content(messageDto.content())
                .timestamp(LocalDateTime.now())
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();

        MessageCorrelationData messageCorrelationData = new MessageCorrelationData(message.id(), message);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), message, messageCorrelationData);
        log.info("A message:{} delivery attempt has been made.", message.id());
    }

    public void enterMessage(MessageDto messageDto) throws AmqpException {

        MessageCorrelationData messageCorrelationData = new MessageCorrelationData(messageDto.id(), messageDto);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto, messageCorrelationData);
    }

    public void exitMessage(MessageDto messageDto) {

        MessageCorrelationData messageCorrelationData = new MessageCorrelationData(messageDto.id(), messageDto);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), messageDto, messageCorrelationData);
    }

    // 메시지 읽음 처리
    public void  markMessageAsRead(Long chatRoomId, Long messageId, Long userId) {
        String key = READ_STATUS_KEY_PREFIX + chatRoomId.toString() + ":" + messageId.toString();
        redisTemplate.opsForSet().add(key, userId);
    }

    // 메시지 읽음 수 조회
    public long getReadCount(Long chatRoomId, Long messageId) {
        String key = READ_STATUS_KEY_PREFIX + chatRoomId.toString() + ":" + messageId.toString();
        return redisTemplate.opsForSet().size(key);
    }
}