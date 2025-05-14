package hello.chat.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka // Kafka 리스너를 활성화
@Configuration
public class KafkaConsumerConfig {

    private final String END_POINT; // Kafka host

    public KafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") String END_POINT) {

        this.END_POINT = END_POINT;
    }

    /**
     * Kafka 리스너를 생성하는 데 사용되는 팩토리
     * 기본 String 타입의 key/value를 처리
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // DefaultKafkaConsumerFactory 생성
        // 이 팩토리는 Kafka 리스너 실행 시 실제 Kafka Consumer 객체를 만들어주는 역할
        // Spring kafka에서 @KafkaListener를 사용할 때, 내부적으로 Kafka Consumer 객체를 만들어줘야 하는데, Kafka Consumer를 만들어주느 역할
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(getDefaultConfigProps()));

        return factory;
    }

    /**
     * Kafka Consumer 설정 프로퍼티를 반환
     * Kafka 브로커 주소, key/value 디시리얼라이저 등을 포함
     */
    private Map<String, Object> getDefaultConfigProps() {

        Map<String, Object> defaultConfigProps = new HashMap<>();

        defaultConfigProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, END_POINT);
        defaultConfigProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        defaultConfigProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return defaultConfigProps;
    }
}