package hello.chat.domain.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final RabbitTemplate rabbitTemplate;
    private final String CHAT_EXCHANGE_NAME;

    public MessageService(RabbitTemplate rabbitTemplate,
                               @Value("${rabbitmq.exchange.name}") String CHAT_EXCHANGE_NAME) {
        this.rabbitTemplate = rabbitTemplate;
        this.CHAT_EXCHANGE_NAME = CHAT_EXCHANGE_NAME;
    }

    public void sendMessage(RabbitMessageDto message) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + message.roomId(), message);
    }
}
