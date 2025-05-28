package hello.chat.domain.chat.dto;

import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * 스프링 AMQP의 CorreltationData를 확장해서, publisher confirm 콜백을 처리할 때 원본 DTO까지 같이 갖고 오기 위해 만든 커스텀 데이터 구조
 * CorrelationData는 기본적으로 ID와 ACK 여부 정보만 확인할 수 있는데, 커스텀함으로써
 * messageDto 필드를 추가할 수 있다.
 */
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
