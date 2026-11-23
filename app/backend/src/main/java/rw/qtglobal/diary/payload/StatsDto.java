package rw.qtglobal.diary.payload;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import rw.qtglobal.diary.entity.Mood;

public record StatsDto(long totalEntries, long entriesThisMonth, int currentStreak, int longestStreak,
                       LocalDate lastEntryDate, Map<Mood, Long> moodBreakdown, List<TagCount> topTags) {
}
