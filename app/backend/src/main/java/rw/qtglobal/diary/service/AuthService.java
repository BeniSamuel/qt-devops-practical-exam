package rw.qtglobal.diary.service;

import rw.qtglobal.diary.payload.AuthResponse;
import rw.qtglobal.diary.payload.LoginRequest;
import rw.qtglobal.diary.payload.RegisterRequest;

public interface AuthService {

    /** Self-registration always creates a USER. */
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
