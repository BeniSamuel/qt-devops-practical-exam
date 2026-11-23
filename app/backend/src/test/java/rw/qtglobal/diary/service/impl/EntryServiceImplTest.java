package rw.qtglobal.diary.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class EntryServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);

    private static List<LocalDate> daysAgo(int... days) {
        return Arrays.stream(days).mapToObj(TODAY::minusDays).toList();
    }

    @Test
    void currentStreakCountsBackFromToday() {
        assertThat(EntryServiceImpl.currentStreak(daysAgo(0, 1, 2, 4), TODAY)).isEqualTo(3);
    }

    @Test
    void currentStreakMayStartYesterday() {
        assertThat(EntryServiceImpl.currentStreak(daysAgo(1, 2), TODAY)).isEqualTo(2);
    }

    @Test
    void currentStreakIsZeroWhenLastEntryIsOld() {
        assertThat(EntryServiceImpl.currentStreak(daysAgo(3, 4), TODAY)).isZero();
    }

    @Test
    void longestStreakFindsBestRun() {
        assertThat(EntryServiceImpl.longestStreak(daysAgo(0, 2, 3, 4, 8))).isEqualTo(3);
    }

    @Test
    void tagsAreNormalized() {
        assertThat(EntryServiceImpl.normalizeTags(List.of(" Work ", "work", "", "Fun,ny"))).isEqualTo("work,funny");
        assertThat(EntryServiceImpl.normalizeTags(List.of())).isNull();
    }
}
