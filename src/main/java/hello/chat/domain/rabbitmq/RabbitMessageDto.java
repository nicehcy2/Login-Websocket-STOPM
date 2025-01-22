package hello.chat.domain.rabbitmq;

public record RabbitMessageDto(
        String roomId,
        String content
) { }
