package rw.qtglobal.diary.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.exception.ApiException;
import rw.qtglobal.diary.payload.AuthResponse;
import rw.qtglobal.diary.payload.LoginRequest;
import rw.qtglobal.diary.payload.RegisterRequest;
import rw.qtglobal.diary.payload.UserDto;
import rw.qtglobal.diary.repository.UserRepository;
import rw.qtglobal.diary.security.JwtService;
import rw.qtglobal.diary.security.UserPrincipal;
import rw.qtglobal.diary.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;

    public AuthServiceImpl(UserRepository users, PasswordEncoder encoder,
                           AuthenticationManager authenticationManager, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "email is already registered");
        }
        User user = users.save(new User(req.fullName().trim(), email, encoder.encode(req.password()), Role.USER));
        return tokenFor(user);
    }

    /**
     * Delegates credential checking to Spring Security (DaoAuthenticationProvider + BCrypt).
     * Bad credentials raise an AuthenticationException, mapped to 401 by GlobalExceptionHandler.
     */
    @Override
    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(normalize(req.email()), req.password()));
        User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        return new AuthResponse(jwt.generate(user), jwt.expiresInSeconds(), UserDto.from(user));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
