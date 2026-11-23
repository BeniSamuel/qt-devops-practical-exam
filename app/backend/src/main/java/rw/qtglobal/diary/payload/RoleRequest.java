package rw.qtglobal.diary.payload;

import jakarta.validation.constraints.NotNull;
import rw.qtglobal.diary.entity.Role;

public record RoleRequest(@NotNull Role role) {
}
