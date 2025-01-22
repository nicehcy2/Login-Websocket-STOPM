package hello.chat.domain.rabbitmq;

public record MessageDto(
        String roomId,
        String content
) { }
