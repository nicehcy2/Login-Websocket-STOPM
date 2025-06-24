package hello.chat.domain.user.controller;

import hello.chat.domain.user.dto.UserRequestDto;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/join")
    public String join(@ModelAttribute UserRequestDto.JoinDto joinDto) {

        return "users/join";
    }

    @PostMapping("/join")
    public String registerUser(@Valid @ModelAttribute UserRequestDto.JoinDto joinDto,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "users/join";
        }

        try {
            userService.saveUser(joinDto);
            return "redirect:/users/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "users/join";
        }
    }

    @GetMapping("/login")
    public String login() {

        return "users/login";
    }
}
