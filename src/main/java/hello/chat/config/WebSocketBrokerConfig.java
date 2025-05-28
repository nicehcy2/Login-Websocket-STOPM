package hello.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final String RABBITMQ_HOST;

    public WebSocketBrokerConfig(
            @Value("${spring.rabbitmq.host}") String rabbitmqHost
    ) {
        this.RABBITMQ_HOST = rabbitmqHost;
    }

    /**
     * STOMP에서 사용하는 메시지 브로커르 설정하는 메소드.
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        /*
        TcpClient tcpClient = TcpClient
                .create()
                .host(RABBITMQ_HOST)
                .port(61613);

        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setAutoStartup(true)
                .setTcpClient(client) // RabbitMQ와 연결할 클라이언트 설정
                .setRelayHost(RABBITMQ_HOST) // RabbitMQ 서버 주소
                .setRelayPort(61613) // RabbitMQ 포트(5672), STOMP(61613)
                .setSystemLogin("guest") // RabbitMQ 시스템 계정
                .setSystemPasscode("guest") // RabbitMQ 시스템 비밀번호
                .setClientLogin("your_username") // RabbitMQ 클라이언트 계정
                .setClientPasscode("your_password"); // RabbitMQ 클라이언트 비밀번호

        registry.setPathMatcher(new AntPathMatcher(".")); // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트에서 메시지 송신 시 프리픽스

         */

        /*
        // 내장 STOMP
        // 내장 메시지 브로커를 사용하기 위한 메소드
        // 메시지를 구독(수신)하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행(송신)하는 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");

         */

        /**
         * RabbitMQ(외부브로커) 사용
         */
        registry.setPathMatcher(new AntPathMatcher(".")); // URL을 / -> .으로
        registry.setApplicationDestinationPrefixes("/pub");  //  @MessageMapping 메서드로 라우팅된다.  Client에서 SEND 요청을 처리
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue");
    }

    /**
     * 웹소켓 configuration의 addHandler 메서드와 유사
     * STOMP를 사용하면 웹 소켓만 사용할 때와 다르게 하나의 연결주소마다 핸들러 클래스를 따로 구현할 필요 없이 Controller 방식으로 간편하게 사용 가능
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 웹 소켓 통신이 /ws으로 도착할 때, 해당 통신이 웹 소켓 통신 중 stomp 통신인 것을 확인하고, 이를 연결.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
                //.withSockJS(); // Jmeter 테스트 시 켜주기
    }
}