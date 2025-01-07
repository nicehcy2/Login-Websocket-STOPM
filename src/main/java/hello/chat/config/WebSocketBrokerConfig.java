package hello.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP에서 사용하는 메시지 브로커르 설정하는 메소드.
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 내장 메시지 브로커를 사용하기 위한 메소드
        // 파라미터로 지정한 prefix(/queue(1대 1) 또는 /topic(1대 다 브로드캐스트))가 붙은 메시지를 발행할 경우, 메시지 브로커가 이를 처리
        registry.enableSimpleBroker("/queue", "/topic");
        // 메시지 핸들러로 라우팅 되는 prefix를 파라미터로 지정 가능. 메시지 가공 처리가 필요한 경우, 가공 핸들러로 메시지를 라우팅 되도록하는 설정.
        registry.setApplicationDestinationPrefixes("/app");

        /*
        // 메시지를 구독(수신)하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행(송신)하는 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");
         */
    }

    /**
     * 웹소켓 configuration의 addHandler 메서드와 유사
     * STOMP를 사용하면 웹 소켓만 사용할 때와 다르게 하나의 연결주소마다 핸들러 클래스를 따로 구현할 필요 없이 Controller 방식으로 간편하게 사용 가능
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 웹 소켓 통신이 /ws/chat으로 도착할 때, 해당 통신이 웹 소켓 통신 중 stomp 통신인 것을 확인하고, 이를 연결.
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins("*");
    }
}
