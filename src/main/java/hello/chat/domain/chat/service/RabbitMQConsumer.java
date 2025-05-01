package hello.chat.domain.chat.service;

import com.rabbitmq.client.Channel;
import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate; // redis

    /*
    @RabbitListener(queues = "chat.queue", ackMode = "MANUAL")
    public void receiveMessage(MessageDto messageDto, Channel channel, Message message) throws IOException {

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

                        // TODO: REDIS 저장 실패 시 메시지 전송도 취소하는 메커니즘이 필요

                        throw e;
                    }
                }
            });

            // 수동 ACK 전송
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {

            log.error("메시지 처리 중 Redis 트랜잭션 실패 또는 기타 예외: {}", e.getMessage(), e);
            // 예외 발생 -> NACK
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    @RabbitListener(queues = "deadLetterQueue")
    public void handleDeadLetter(MessageDto dto) {
        log.info("dead message = {}", dto.id());
    }

     */
}
