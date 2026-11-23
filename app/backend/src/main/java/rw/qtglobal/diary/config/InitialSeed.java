package rw.qtglobal.diary.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import rw.qtglobal.diary.repository.UserRepository;
import rw.qtglobal.diary.service.SeedService;

/**
 * Creates the test accounts the first time the API starts on an empty database, so a fresh deployment is
 * usable immediately. Later restarts do nothing. There is no seed endpoint. Disabled with SEED_ENABLED=false.
 * Runs before the application reports itself ready.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class InitialSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialSeed.class);

    private final UserRepository users;
    private final SeedService seed;

    public InitialSeed(UserRepository users, SeedService seed) {
        this.users = users;
        this.seed = seed;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (users.count() > 0) {
                return;
            }
            Map<String, Object> result = seed.seed();
            log.info("First start: created {} test accounts and {} sample entries (see README)",
                    result.get("usersCreated"), result.get("entriesCreated"));
        } catch (RuntimeException e) {
            log.warn("Initial seed failed: {}", e.getMessage());
        }
    }
}
