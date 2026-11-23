package rw.qtglobal.diary.payload;

public record AuthResponse(String token, long expiresIn, UserDto user) {
}
