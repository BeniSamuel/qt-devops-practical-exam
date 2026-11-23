package rw.qtglobal.diary.payload;

import java.time.Instant;

import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;

public record UserDto(Long id, String fullName, String email, Role role, Instant createdAt) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getCreatedAt());
    }
}
