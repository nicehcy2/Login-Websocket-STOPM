package hello.chat.config;


import hello.chat.domain.chat.dto.MessageCorrelationData;
import hello.chat.domain.chat.dto.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class TestRabbitConfirmCallbackConfig {

    public void registerConfirmCallback(RabbitTemplate rabbitTemplate) {

        // ReturnCallback 설정 - 해당 exchange에 바인됭 queue가 없거나 routingKey가 잘못되었다면, 콜백 메서드가 실행 됩니다.
        // 브로커는 메시지를 받았지만, 큐에 라우팅 실패했을 때 호출
        rabbitTemplate.setReturnsCallback(returned -> {
            // 반환된 메시지 처리
            /*
            log.error("Message could not be routed. ReplyCode: {}, ReplyText: {}, Exchange: {}, RoutingKey: {}",
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey());

             */
        });

        // RabbitTemplate에 confirm callback을 설정합니다.
        // Publisher Confirms 기능이 활성화된 상태에서 RabbitMQ 서버가 메시지를 성공적으로 받았는지 여부를 확인할 때 사용됩니다.
        // 브로커가 메시지를 수신(ack) 또는 거부(nack) 했을 때 호출
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {

            // 메시지가 성공적으로 RabbitMQ 서버에 전달되었을 때
            if (ack) {
                //log.info("Message successfully sent to broker");
            } else { // 메시지가 RabbitMQ 서버에 전달되지 못했을 때
                //log.error("Message failed to send to broker: {}, cause: {}", correlationData.getId(), cause);

                // correlationData에서 messageDto 꺼내기
                if (correlationData instanceof MessageCorrelationData mcd) {

                    MessageDto messageDto = mcd.getMessageDto();

                    MessageDto updatedMessageDto = MessageDto.builder()
                            .id(messageDto.id())
                            .chatRoomId(messageDto.chatRoomId())
                            .senderId(messageDto.senderId())
                            .messageType(messageDto.messageType())
                            .content(messageDto.content())
                            .timestamp(messageDto.timestamp())
                            .unreadCount(messageDto.unreadCount())
                            .publishRetryCount(messageDto.publishRetryCount() + 1)
                            .saveStatus(messageDto.saveStatus())
                            .build();

                    //log.info("Attempting to resend message: {}", updatedMessageDto);

                    // 최대 3 번까지만 재전송 가능
                    if (updatedMessageDto.publishRetryCount() < 3) {
                        MessageCorrelationData retryCorrelationData = new MessageCorrelationData(updatedMessageDto.id(), updatedMessageDto);
                        rabbitTemplate.convertAndSend("exchange", "routing.key", updatedMessageDto,
                                msg -> {
                                    msg.getMessageProperties().setExpiration("1000");
                                    return msg;
                                },
                                retryCorrelationData);
                    }
                }
            }
        });
    }
}
