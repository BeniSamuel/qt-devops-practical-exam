package rw.qtglobal.diary.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import rw.qtglobal.diary.config.AppProperties;
import rw.qtglobal.diary.entity.User;

@Service
public class JwtService {

    static final String ISSUER = "mydiary-api";

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(AppProperties props) {
        String secret = props.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be set and at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofMinutes(props.jwt().expirationMinutes());
    }

    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(ISSUER)
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }

    /** Returns the user id if the token is valid (canonical encoding, signature, issuer, expiry). */
    public Optional<Long> parseUserId(String token) {
        if (!isCanonical(token)) {
            return Optional.empty();
        }
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.valueOf(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Base64url decoders ignore the unused low bits of the last character, so several spellings of the same
     * token would otherwise be accepted. Re-encoding each segment ensures exactly one valid encoding.
     */
    static boolean isCanonical(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            for (String part : parts) {
                byte[] bytes = Base64.getUrlDecoder().decode(part);
                if (!Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(part)) {
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
