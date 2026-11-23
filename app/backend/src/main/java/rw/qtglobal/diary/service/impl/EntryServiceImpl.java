package rw.qtglobal.diary.service.impl;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.qtglobal.diary.config.CacheConfig;
import rw.qtglobal.diary.entity.DiaryEntry;
import rw.qtglobal.diary.entity.Mood;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.exception.ApiException;
import rw.qtglobal.diary.payload.EntryDto;
import rw.qtglobal.diary.payload.EntryRequest;
import rw.qtglobal.diary.payload.PageResponse;
import rw.qtglobal.diary.payload.StatsDto;
import rw.qtglobal.diary.payload.TagCount;
import rw.qtglobal.diary.repository.DiaryEntryRepository;
import rw.qtglobal.diary.service.EntryService;

@Service
public class EntryServiceImpl implements EntryService {

    private final DiaryEntryRepository entries;

    public EntryServiceImpl(DiaryEntryRepository entries) {
        this.entries = entries;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EntryDto> list(User owner, String q, Mood mood, String tag, LocalDate from, LocalDate to,
                                       int page, int size) {
        Specification<DiaryEntry> spec = (root, query, cb) -> cb.equal(root.get("user").get("id"), owner.getId());
        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("content")), pattern)));
        }
        if (mood != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mood"), mood));
        }
        if (tag != null && !tag.isBlank()) {
            String pattern = "%," + tag.trim().toLowerCase() + ",%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.concat(cb.concat(",", root.get("tags")), ","), pattern));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("entryDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("entryDate"), to));
        }
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Order.desc("entryDate"), Sort.Order.desc("createdAt")));
        return PageResponse.from(entries.findAll(spec, pageable).map(EntryDto::from));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ENTRIES, key = "#owner.id + ':' + #id")
    public EntryDto get(User owner, Long id) {
        return EntryDto.from(find(owner, id));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.ENTRY_STATS, key = "#owner.id")
    public EntryDto create(User owner, EntryRequest req) {
        DiaryEntry entry = new DiaryEntry(owner);
        apply(entry, req);
        return EntryDto.from(entries.save(entry));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = CacheConfig.ENTRIES, key = "#owner.id + ':' + #id"),
            evict = @CacheEvict(cacheNames = CacheConfig.ENTRY_STATS, key = "#owner.id"))
    public EntryDto update(User owner, Long id, EntryRequest req) {
        DiaryEntry entry = find(owner, id);
        apply(entry, req);
        return EntryDto.from(entries.saveAndFlush(entry));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ENTRIES, key = "#owner.id + ':' + #id"),
            @CacheEvict(cacheNames = CacheConfig.ENTRY_STATS, key = "#owner.id")})
    public void delete(User owner, Long id) {
        entries.delete(find(owner, id));
    }

    /** Cached for app.cache.stats-ttl; evicted whenever the owner's entries change. */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ENTRY_STATS, key = "#owner.id")
    public StatsDto stats(User owner) {
        Long uid = owner.getId();
        LocalDate today = LocalDate.now();

        Map<Mood, Long> moods = new EnumMap<>(Mood.class);
        for (Mood m : Mood.values()) {
            moods.put(m, 0L);
        }
        for (Object[] row : entries.countByMood(uid)) {
            moods.put((Mood) row[0], (Long) row[1]);
        }

        Map<String, Long> tagCounts = new TreeMap<>();
        for (String tags : entries.findTags(uid)) {
            for (String t : tags.split(",")) {
                if (!t.isBlank()) {
                    tagCounts.merge(t, 1L, Long::sum);
                }
            }
        }
        List<TagCount> topTags = tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> new TagCount(e.getKey(), e.getValue()))
                .toList();

        List<LocalDate> dates = entries.findEntryDates(uid); // distinct, newest first
        return new StatsDto(
                entries.countByUserId(uid),
                entries.countByUserIdAndEntryDateBetween(uid, today.withDayOfMonth(1), today),
                currentStreak(dates, today),
                longestStreak(dates),
                dates.isEmpty() ? null : dates.get(0),
                moods,
                topTags);
    }

    // ---- helpers ------------------------------------------------------------

    private DiaryEntry find(User owner, Long id) {
        return entries.findByIdAndUserId(id, owner.getId())
                .orElseThrow(() -> ApiException.notFound("diary entry not found"));
    }

    private static void apply(DiaryEntry entry, EntryRequest req) {
        entry.setTitle(req.title().trim());
        entry.setContent(req.content().trim());
        entry.setMood(req.mood());
        entry.setEntryDate(req.entryDate());
        entry.setTags(normalizeTags(req.tags()));
    }

    static String normalizeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }
        Set<String> clean = tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase().replace(",", ""))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return clean.isEmpty() ? null : String.join(",", clean);
    }

    /** Consecutive days with an entry, ending today (or yesterday, if today has no entry yet). */
    static int currentStreak(List<LocalDate> datesDesc, LocalDate today) {
        if (datesDesc.isEmpty()) {
            return 0;
        }
        LocalDate expected = datesDesc.get(0).equals(today) ? today : today.minusDays(1);
        int streak = 0;
        for (LocalDate d : datesDesc) {
            if (d.isAfter(expected)) {
                continue; // future-dated entries are ignored
            }
            if (!d.equals(expected)) {
                break;
            }
            streak++;
            expected = expected.minusDays(1);
        }
        return streak;
    }

    static int longestStreak(List<LocalDate> datesDesc) {
        int best = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : datesDesc) {
            run = (prev != null && d.equals(prev.minusDays(1))) ? run + 1 : 1;
            best = Math.max(best, run);
            prev = d;
        }
        return best;
    }
}
