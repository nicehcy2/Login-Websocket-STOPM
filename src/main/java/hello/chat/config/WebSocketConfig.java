package hello.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/*
@Configuration
@EnableWebSocket // Spring에서 WebSocket을 활성화한다. WebSocketConfigurer 인터페이스를 구현한 클래스를 통해 WebSocket 설정을 제공
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    // WebSocket 통신을 처리하는 로직이 구현된 핸들러. 클라이언트와 서버 간 메시지 송수신을 담당.
    private final WebSocketHandler webSocketHandler;

    */
/**
     * WebSocket 핸들러를 특정 엔드포인트와 연결하는 메서드
     * @param registry WebSocket 엔드포인트를 등록하는데 사용
     *//*

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 이를 통해서 ws://localhost:9090/ws/chat 으로 요청이 들어오면 websocket 통신을 진행한다.
        // setAllowedOrigins("*")는 모든 ip에서 접속 가능하도록 해줌
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOrigins("*");
    }
}
*/
