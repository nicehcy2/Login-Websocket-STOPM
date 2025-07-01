package hello.chat.domain.chat.entity;

import hello.chat.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 제목
    @Column(name = "title", nullable = false, length = 18)
    private String title;

    // 최대 참여인원 - 최대 100명
    @Column(name = "max_participants", nullable = false)
    @Max(100)
    private Integer maxParticipants;

    // 참여 인원 수 - 최대 maxParticipants명
    @Column(name = "participation_count", nullable = false)
    @Min(0) // 음수가 될 수 없도록
    private Integer participationCount;

    // 프로필 이미지
    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatRoomMembership> chatRoomMembershipList;
}