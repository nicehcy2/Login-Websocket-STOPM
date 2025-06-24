package hello.chat.domain.user.service;

import hello.chat.domain.user.dto.UserRequestDto;

public interface UserService {

    void saveUser(UserRequestDto.JoinDto joinDto);
}
