package rw.qtglobal.diary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.service.AuthService;
import rw.qtglobal.diary.security.CurrentUser;
import rw.qtglobal.diary.payload.AuthResponse;
import rw.qtglobal.diary.payload.LoginRequest;
import rw.qtglobal.diary.payload.MeResponse;
import rw.qtglobal.diary.payload.RegisterRequest;
import rw.qtglobal.diary.payload.UserDto;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    public MeResponse me(@CurrentUser User user) {
        return new MeResponse(UserDto.from(user));
    }
}
