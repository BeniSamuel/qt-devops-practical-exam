package rw.qtglobal.diary.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Seed seed, Cache cache) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Seed(boolean enabled) {
    }

    public record Cache(Duration statsTtl, Duration entryTtl) {
    }
}
