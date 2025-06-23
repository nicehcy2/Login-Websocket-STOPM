package hello.chat.domain.chat.controller;

import hello.chat.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("users")
public class UserController {

    @GetMapping("/add")
    public String addForm(@ModelAttribute User user) {

        return "users/addUserForm";
    }
}
