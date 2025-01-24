/*
package hello.chat.config;

import hello.chat.domain.chat.dto.MessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


*/
/**
 * Asynchronous RabbitMQConsumer 설정 -> 이를 활용해서
 * 메시지 저장을 비동기 처리
 * 다른 서비스(푸시 알람)과 연동을 위한 메시지 임시 저장 및 비동기 처리 가능
 * 오프라인 메시지 관리 가능
 *//*

@Component
public class RabbitMQConsumer {

    @RabbitListener(queues = "chat.queue")
    public void receiveMessage(MessageDto messageDto) {
        System.out.println("Received Message: " + messageDto);
    }
}
*/
