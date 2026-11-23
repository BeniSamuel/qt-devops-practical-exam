package rw.qtglobal.diary.payload;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import rw.qtglobal.diary.entity.Mood;

public record EntryRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 20000) String content,
        @NotNull Mood mood,
        @Size(max = 10, message = "at most 10 tags") List<@Size(max = 30) String> tags,
        @NotNull @PastOrPresent(message = "cannot be in the future") LocalDate entryDate) {
}
