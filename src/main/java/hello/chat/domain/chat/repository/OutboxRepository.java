package hello.chat.domain.chat.repository;

import hello.chat.domain.chat.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
}
