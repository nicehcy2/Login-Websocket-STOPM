package hello.chat.domain.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService chatMessageProducer;

    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, RabbitMessageDto message) {
        log.info("sendMessage: roomId={}, message={}", roomId, message);
        chatMessageProducer.sendMessage(message);
    }
}
