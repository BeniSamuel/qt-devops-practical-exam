package rw.qtglobal.diary.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.qtglobal.diary.config.CacheConfig;
import rw.qtglobal.diary.entity.DiaryEntry;
import rw.qtglobal.diary.entity.Mood;
import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.repository.DiaryEntryRepository;
import rw.qtglobal.diary.repository.UserRepository;
import rw.qtglobal.diary.service.SeedService;

@Service
public class SeedServiceImpl implements SeedService {

    /** Demo accounts, documented in the README ("Test accounts"). Passwords are never returned by the API. */
    private record SeedUser(String fullName, String email, String password, Role role) {
    }

    /** Summary of a created account, logged at startup (never includes the password). */
    public record SeedAccount(String fullName, String email, Role role) {
    }

    private static final List<SeedUser> SEED_USERS = List.of(
            new SeedUser("Alice Admin", "admin@qt.rw", "Admin@123", Role.ADMIN),
            new SeedUser("Demo Writer", "demo@qt.rw", "Demo@123", Role.USER),
            new SeedUser("Jane Writer", "jane@qt.rw", "Jane@123", Role.USER));

    private record Sample(int daysAgo, String title, String content, Mood mood, String tags) {
    }

    private static final Map<String, List<Sample>> SAMPLES = Map.of(
            "demo@qt.rw", List.of(
                    new Sample(0, "Kubernetes finally clicked",
                            "Spent the afternoon on Deployments and Services. The cluster is up and the pods are healthy.",
                            Mood.GREAT, "work,kubernetes,learning"),
                    new Sample(1, "Rainy Sunday",
                            "Stayed in, read a book and cooked isombe. Slow but good day.",
                            Mood.GOOD, "family,food"),
                    new Sample(2, "Docker Compose debugging",
                            "The API container could not reach the database. The service name was wrong.",
                            Mood.OKAY, "work,docker"),
                    new Sample(4, "Morning run around Kigali",
                            "Ran 7 km before work. The hills are tough but the view is worth it.",
                            Mood.GREAT, "health,running"),
                    new Sample(6, "Missed a deadline",
                            "The Ansible playbook was not idempotent and broke the staging cluster. Lesson learned.",
                            Mood.BAD, "work,ansible")),
            "jane@qt.rw", List.of(
                    new Sample(0, "Jane's private note", "Only Jane can read this entry.", Mood.GOOD, "private"),
                    new Sample(3, "Weekend plans", "Visit Lake Kivu with friends.", Mood.GREAT, "travel")));

    /** PostgreSQL advisory-lock key: API replicas starting together seed one after another, never twice. */
    private static final long SEED_LOCK_KEY = 0x5EED_0001L;

    private final UserRepository users;
    private final DiaryEntryRepository entries;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;

    public SeedServiceImpl(UserRepository users, DiaryEntryRepository entries, PasswordEncoder encoder,
                           JdbcTemplate jdbc) {
        this.users = users;
        this.entries = entries;
        this.encoder = encoder;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.ENTRY_STATS, CacheConfig.ENTRIES}, allEntries = true)
    public Map<String, Object> seed() {
        // Held until the transaction ends; a second replica waits here, then finds everything already created.
        jdbc.queryForList("select pg_advisory_xact_lock(?)", SEED_LOCK_KEY);
        int usersCreated = 0;
        int entriesCreated = 0;
        for (SeedUser su : SEED_USERS) {
            User user = users.findByEmailIgnoreCase(su.email()).orElse(null);
            if (user == null) {
                user = users.save(new User(su.fullName(), su.email(), encoder.encode(su.password()), su.role()));
                usersCreated++;
            }
            if (entries.countByUserId(user.getId()) == 0) {
                List<DiaryEntry> batch = new ArrayList<>();
                for (Sample s : SAMPLES.getOrDefault(su.email(), List.of())) {
                    DiaryEntry e = new DiaryEntry(user);
                    e.setTitle(s.title());
                    e.setContent(s.content());
                    e.setMood(s.mood());
                    e.setTags(s.tags());
                    e.setEntryDate(LocalDate.now().minusDays(s.daysAgo()));
                    batch.add(e);
                }
                entries.saveAll(batch);
                entriesCreated += batch.size();
            }
        }
        return Map.of(
                "message", "seed completed",
                "usersCreated", usersCreated,
                "entriesCreated", entriesCreated,
                "users", SEED_USERS.stream().map(u -> new SeedAccount(u.fullName(), u.email(), u.role())).toList());
    }
}
