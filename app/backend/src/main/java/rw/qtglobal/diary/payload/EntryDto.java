package rw.qtglobal.diary.payload;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import rw.qtglobal.diary.entity.DiaryEntry;
import rw.qtglobal.diary.entity.Mood;

public record EntryDto(Long id, String title, String content, Mood mood, List<String> tags,
                       LocalDate entryDate, Instant createdAt, Instant updatedAt) {

    public static EntryDto from(DiaryEntry e) {
        List<String> tags = e.getTags() == null || e.getTags().isBlank()
                ? List.of()
                : Arrays.asList(e.getTags().split(","));
        return new EntryDto(e.getId(), e.getTitle(), e.getContent(), e.getMood(), tags,
                e.getEntryDate(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
