package hello.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@Slf4j
public class RabbitMQConfig {

    private final String CHAT_QUEUE_NAME; // RabbitMQ Queue 이름
    private final String CHAT_EXCHANGE_NAME; // RabbitMQ Exchange 이름
    private final String CHAT_ROUTING_KEY; // RabbitMQ Binding 이름, TopicExchange를 사용하기에 Binding이 routing 역할을 수행하도록 한다.
    private final String RABBITMQ_HOST;
    private final CachingConnectionFactory.ConfirmType PUBLISHER_CONFIRM_TYPE;
    private final boolean PUBLISHER_RETURNS;
    private final int RABBITMQ_PORT;
    private final String RABBITMQ_USERNAME;
    private final String RABBITMQ_PASSWORD;


    public RabbitMQConfig(
            @Value("${rabbitmq.queue.name}") String CHAT_QUEUE_NAME,
            @Value("${rabbitmq.exchange.name}") String CHAT_EXCHANGE_NAME,
            @Value("${rabbitmq.routing.key}") String CHAT_ROUTING_KEY,
            @Value("${spring.rabbitmq.host}") String RABBITMQ_HOST,
            @Value("${spring.rabbitmq.publisher-confirm-type}") CachingConnectionFactory.ConfirmType PUBLISHER_CONFIRM_TYPE,
            @Value("{spring.rabbitmq.publisher-returns}") boolean PUBLISHER_RETURNS,
            @Value("${spring.rabbitmq.port}") int RABBITMQ_PORT,
            @Value("${spring.rabbitmq.username}") String RABBITMQ_USERNAME,
            @Value("${spring.rabbitmq.password}") String RABBITMQ_PASSWORD
    ) {

        this.CHAT_QUEUE_NAME = CHAT_QUEUE_NAME;
        this.CHAT_EXCHANGE_NAME = CHAT_EXCHANGE_NAME;
        this.CHAT_ROUTING_KEY = CHAT_ROUTING_KEY;
        this.RABBITMQ_HOST = RABBITMQ_HOST;
        this.PUBLISHER_CONFIRM_TYPE = PUBLISHER_CONFIRM_TYPE;
        this.PUBLISHER_RETURNS = PUBLISHER_RETURNS;
        this.RABBITMQ_PORT = RABBITMQ_PORT;
        this.RABBITMQ_USERNAME = RABBITMQ_USERNAME;
        this.RABBITMQ_PASSWORD = RABBITMQ_PASSWORD;
    }

    /**
     * @return "chat.queue"라는 이름의 Queue 생성
     */
    @Bean
    public Queue chatQueue() {

        return new Queue(CHAT_QUEUE_NAME, true); // durable을 true로 제공
    }

    /**
     * TopicExchange: 메시지의 Routing Key와 Binding Key를 패턴 매칭을 통해 비교하여 메시지를 라우팅하는 역할
     *                메시지의 Routing Key와 Binding Key가 일치하면, 해당 메시지가 연결된 Queue로 전달됨.
     *
     * @return "chat.exchange"라는 이름의 Exchange 생성
     */
    @Bean
    public TopicExchange chatExchange() {

        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }

    /**
     * Exchange와 Queue를 특정 라우팅 키로 연결하는 역할.
     *
     * @param chatQueue "chat.queue"
     * @param chatExchange TopicExchage "chat.exchange", 메시지를 특정 패턴(Routing Key)에 따라 라우팅하는 역할
     * @return "chat.queue"에 "chat.exchange" 규칙을 Binding한다.
     */
    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {

        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with(CHAT_ROUTING_KEY);
    }

    /**
     * @return RabbitMQ로 메시지를 송신하거나 수신할 때 사용하는 템플릿 클래스를 생성
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(createConnectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());

        rabbitTemplate.setMandatory(true); // 이 설정이 true일 때, 메시지가 라우팅되지 않으면 ReturnsCallback이 호출됩니다.

        // ReturnCallback 설정 - 해당 exchange에 바인됭 queue가 없거나 routingKey가 잘못되었다면, 콜백 메서드가 실행 됩니다.
        // 브로커는 메시지를 받았지만, 큐에 라우팅 실패했을 때 호출
        rabbitTemplate.setReturnsCallback(returned -> {
            // 반환된 메시지 처리
            log.error("Message could not be routed. ReplyCode: {}, ReplyText: {}, Exchange: {}, RoutingKey: {}",
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey());
        });

        // RabbitTemplate에 confirm callback을 설정합니다.
        // Publisher Confirms 기능이 활성화된 상태에서 RabbitMQ 서버가 메시지를 성공적으로 받았는지 여부를 확인할 때 사용됩니다.
        // 브로커가 메시지를 수신(ack) 또는 거부(nack) 했을 때 호출
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {

            // 메시지가 성공적으로 RabbitMQ 서버에 전달되었을 때
            if (ack) {
                log.info("Message successfully sent to broker");
            } else { // 메시지가 RabbitMQ 서버에 전달되지 못했을 때
                log.error("Message failed to send to broker: {}, cause: {}", correlationData.getId(), cause);
                // TODO: 재전송 로직
            }
        });

        return rabbitTemplate;
    }

    /**
     * CachingConnectionFactory: RabbitMQ와의 연결을 효율적으로 관리하기 위해 커넥션 풀링 및 캐싱 기능을 제공
     *
     * @return RabbitMQ와의 연결을 관리하는 ConnectionFactory를 생성
     */
    @Bean
    public ConnectionFactory createConnectionFactory() {

        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBITMQ_USERNAME); // RabbitMQ 관리자 아이디
        factory.setPassword(RABBITMQ_PASSWORD); // RabbitMQ 관리자 비밀번호
        factory.setPort(RABBITMQ_PORT); // RabbitMQ 연결할 port
        factory.setVirtualHost("/"); // vhost 지정

        factory.setPublisherConfirmType(PUBLISHER_CONFIRM_TYPE); // 채널에서 publisher confirm을 활성화하면, 클라이언트가 발행한 메시지에 대해 브로커가 비동기적으로 확인 응답을 보냅니다.
        factory.setPublisherReturns(PUBLISHER_RETURNS); // 메시지가 큐에 도달하지 못했을 때 콜백으로 알림을 받을 수 있도록 설정

        return factory;
    }

    /**
     * @param connectionFactory RabbitMQ와의 연결을 관리하는 연결 팩토리
     * @return RabbitMQ에 자동으로 자원을 등록
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {

        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareExchange(chatExchange());
        rabbitAdmin.declareQueue(chatQueue());
        rabbitAdmin.declareBinding(chatBinding(chatQueue(), chatExchange()));

        return rabbitAdmin;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter(); // 메시지를 JSON으로 직렬/역직렬화
    }

    /**
     * @RabbitListener 애노테이션이 붙은 메소드가 메시지를 비동기적으로 처리할 수 있게 해주는 리스너 컨테이너를 설정
     * 메시지가 큐에 도착하면, SimpleRabbitListenerContainerFactory가 자동으로 리스너 컨테이너를 관리하여 메시지를 수신하고 처리
     *
     * @param connectionFactory RabbitMQ 연결 팩토리
     * @param messageConverter Json 데이터 직렬화/역직렬화
     */
    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        factory.setPrefetchCount(100); // 한 번에 100개만 처리 가능

        return factory;
    }
}