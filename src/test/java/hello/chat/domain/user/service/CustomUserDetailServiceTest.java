package hello.chat.domain.user.service;

import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.entity.UserRole;
import hello.chat.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository; // UserRepository를 가짜(Mock) 객체로 만듦

    @InjectMocks
    private CustomUserDetailService userDetailService; // userRepository를 주입받는 실제 테스트 대상 객체

    @Test
    void loadUserByUsername_사용자가_존재할_경우() {

        // given - 사용자 정보가 DB에 존재하는 경우를 가정
        User user = User.builder()
                .name("김광현")
                .age(38)
                .email("1234@naver.com")
                .password("1234")
                .role(UserRole.USER)
                .build();

        /**
         * doReturn(...).when(...).method(...)
         * doReturn(리턴할 값).when(가짜 객체).메서드(인자)
         *
         * 가짜 객체가 특정한 값을 반환해야 하는 경우
         */
        Mockito.doReturn(Optional.of(user)).when(userRepository)
                .findByEmail("1234@naver.com");

        // when - 해당 이메일로 사용자 조회 시도
        UserDetails userDetails = userDetailService.loadUserByUsername("1234@naver.com");

        // then - 결과 검증
        assertEquals("1234@naver.com", userDetails.getUsername());
        assertEquals("1234", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_사용자가_없을_경우() {

        // given
        Mockito.doReturn(Optional.empty()).when(userRepository)
                .findByEmail("notfound@naver.com");

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailService.loadUserByUsername("notfound@naver.com");
        });
    }
}