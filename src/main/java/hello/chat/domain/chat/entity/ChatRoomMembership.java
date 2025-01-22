package hello.chat.domain.chat.entity;

import hello.chat.domain.common.BaseEntity;
import hello.chat.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
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
