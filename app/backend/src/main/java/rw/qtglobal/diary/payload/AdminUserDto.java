package rw.qtglobal.diary.payload;

import java.time.Instant;

import rw.qtglobal.diary.entity.Role;

public record AdminUserDto(Long id, String fullName, String email, Role role, Instant createdAt, long entryCount) {
}
