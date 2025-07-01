package hello.chat.domain.chat.entity;

import hello.chat.common.BaseEntity;
import hello.chat.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Builder
public class ChatRoomMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;
}