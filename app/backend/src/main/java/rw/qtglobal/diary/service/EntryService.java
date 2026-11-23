package rw.qtglobal.diary.service;

import java.time.LocalDate;

import rw.qtglobal.diary.entity.Mood;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.payload.EntryDto;
import rw.qtglobal.diary.payload.EntryRequest;
import rw.qtglobal.diary.payload.PageResponse;
import rw.qtglobal.diary.payload.StatsDto;

/**
 * Every operation is scoped to the given owner: a diary entry is only ever visible to its author.
 * Entries that belong to someone else are reported as 404 so their existence is not leaked.
 */
public interface EntryService {

    PageResponse<EntryDto> list(User owner, String q, Mood mood, String tag, LocalDate from, LocalDate to,
                                int page, int size);

    EntryDto get(User owner, Long id);

    EntryDto create(User owner, EntryRequest request);

    EntryDto update(User owner, Long id, EntryRequest request);

    void delete(User owner, Long id);

    StatsDto stats(User owner);
}
