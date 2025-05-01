package hello.chat.domain.chat.service;

import com.github.f4b6a3.tsid.TsidCreator;
import hello.chat.domain.chat.dto.MessageCorrelationData;
import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RedisTemplate<String, Object> redisTemplate; // redis
    private final RabbitTemplate rabbitTemplate; // rabbitmq
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Value("${rabbitmq.exchange.name}") private String CHAT_EXCHANGE_NAME;
    private static final String READ_STATUS_KEY_PREFIX = "read:message:";

    public List<MessageDto> findMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messgeList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messgeList.stream()
                .map(object -> (MessageDto) object)
                .toList();
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
                .timestamp(String.valueOf(LocalDateTime.now()))
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();

        // publisher confirm 콜백을 받을 때 어떤 메시지에 대한 확인인지 식별하기 위한 용도
        // 메시지의 고유 ID나 메타데이터를 담아서, ConfirmCallback이 호출될 때 ConfirmCallback 파라미터로 똑같이 돌아오므로, 메시지 추적 가능
        MessageCorrelationData messageCorrelationData = new MessageCorrelationData(message.id(), message);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + messageDto.chatRoomId(), message,
                msg -> { // MessagePostProcessor로 메시지를 보내기 전 메시지를 가공
                    msg.getMessageProperties().setExpiration("5000"); // expiration을 설정해서 메시지 만료 설정(TTL 설정)
                    return msg;
                },
                messageCorrelationData);
        log.info("A message:{} delivery attempt has been made.", message.id());
    }

    public void saveMessage(MessageDto messageDto) {

        try {
            userRepository.findById(messageDto.senderId()).orElseThrow(RuntimeException::new);
            chatRoomRepository.findById(messageDto.chatRoomId()).orElseThrow(RuntimeException::new);

            String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";

            redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                    try {
                        operations.multi(); // Redis 트랜잭션 시작
                        operations.opsForList().leftPush((K) redisKey, (V) messageDto);

                        return operations.exec(); // Redis 트랜잭션 실행 (실제 저장)
                    } catch (RuntimeException e) {
                        operations.discard(); // 예외 발생 시 트랜잭션 롤백

                        throw e;
                    }
                }
            });
        } catch (Exception e) {

            log.error("메시지 처리 중 Redis 트랜잭션 실패 또는 기타 예외: {}", e.getMessage(), e);
        }
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