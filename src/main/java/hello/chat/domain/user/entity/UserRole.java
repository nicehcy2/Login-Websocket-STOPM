package hello.chat.domain.user.entity;

public enum UserRole {
    AMDIN("관리자"),
    USER("사용자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
