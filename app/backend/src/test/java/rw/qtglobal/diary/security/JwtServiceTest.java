package rw.qtglobal.diary.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import rw.qtglobal.diary.config.AppProperties;
import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;

class JwtServiceTest {

    private static AppProperties props(String secret, long minutes) {
        return new AppProperties(new AppProperties.Jwt(secret, minutes),
                new AppProperties.Cors(List.of("*")), new AppProperties.Seed(true),
                new AppProperties.Cache(Duration.ofMinutes(10), Duration.ofMinutes(30)));
    }

    private static User user(long id) throws Exception {
        User u = new User("Test", "test@qt.rw", "hash", Role.USER);
        Field f = User.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(u, id);
        return u;
    }

    @Test
    void generatedTokenIsAccepted() throws Exception {
        JwtService jwt = new JwtService(props("0123456789abcdef0123456789abcdef", 60));
        assertThat(jwt.parseUserId(jwt.generate(user(42)))).contains(42L);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() throws Exception {
        String token = new JwtService(props("0123456789abcdef0123456789abcdef", 60)).generate(user(1));
        JwtService other = new JwtService(props("ffffffffffffffffffffffffffffffff", 60));
        assertThat(other.parseUserId(token)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        JwtService jwt = new JwtService(props("0123456789abcdef0123456789abcdef", -1));
        assertThat(jwt.parseUserId(jwt.generate(user(1)))).isEmpty();
    }

    @Test
    void tamperedSignatureIsRejected() throws Exception {
        JwtService jwt = new JwtService(props("0123456789abcdef0123456789abcdef", 60));
        String token = jwt.generate(user(7));
        int i = token.length() - 10;
        String tampered = token.substring(0, i) + (token.charAt(i) == 'A' ? 'B' : 'A') + token.substring(i + 1);
        assertThat(jwt.parseUserId(tampered)).isEmpty();
    }

    @Test
    void nonCanonicalEncodingOfAValidTokenIsRejected() throws Exception {
        JwtService jwt = new JwtService(props("0123456789abcdef0123456789abcdef", 60));
        String token = jwt.generate(user(7));
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        int last = alphabet.indexOf(token.charAt(token.length() - 1));
        // flipping the lowest bit of the last character only changes unused padding bits
        String variant = token.substring(0, token.length() - 1) + alphabet.charAt(last ^ 1);
        assertThat(jwt.parseUserId(token)).contains(7L);
        assertThat(jwt.parseUserId(variant)).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService(props("too-short", 60)))
                .isInstanceOf(IllegalStateException.class);
    }
}
