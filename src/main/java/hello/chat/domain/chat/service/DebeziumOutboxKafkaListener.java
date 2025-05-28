package hello.chat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DebeziumOutboxKafkaListener {

    private final ChatService chatService;

    @KafkaListener(topics = "outbox_db_server1.testdb.outbox", groupId = "test-topic-group-01")
    public void sendTest(ConsumerRecord<String, String> record) throws Exception {

        chatService.saveMessage(DebeziumMessageParser.parse(record.value()));
        System.out.println("send message : " + record.value());
    }
}
