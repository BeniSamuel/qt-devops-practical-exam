package rw.qtglobal.diary.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public health check under /api, so it is reachable through the reverse proxy like the rest of the API.
 * 200 while PostgreSQL is reachable, 503 otherwise. The Redis cache is optional: its status is reported
 * but never makes the check fail. Kubernetes probes can keep using /actuator/health/liveness and /readiness.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthEndpoint health;
    private final boolean cacheDisabled;

    public HealthController(HealthEndpoint health, @Value("${spring.cache.type:redis}") String cacheType) {
        this.health = health;
        this.cacheDisabled = "none".equalsIgnoreCase(cacheType);
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> check() {
        boolean databaseUp = isUp("db");
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", databaseUp ? "ok" : "error");
        body.put("database", databaseUp ? "up" : "down");
        body.put("cache", cacheDisabled ? "disabled" : (isUp("redis") ? "up" : "down"));
        return ResponseEntity.status(databaseUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean isUp(String component) {
        HealthComponent result = health.healthForPath(component);
        return result != null && Status.UP.equals(result.getStatus());
    }
}
