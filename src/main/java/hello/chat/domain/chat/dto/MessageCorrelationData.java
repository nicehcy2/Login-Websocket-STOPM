package hello.chat.domain.chat.dto;

import org.springframework.amqp.rabbit.connection.CorrelationData;

public class MessageCorrelationData extends CorrelationData {

    private final MessageDto messageDto;

    public MessageCorrelationData(String id, MessageDto messageDto) {
        super(id);
        this.messageDto = messageDto;
    }

    public MessageDto getMessageDto() {
        return messageDto;
    }
}
