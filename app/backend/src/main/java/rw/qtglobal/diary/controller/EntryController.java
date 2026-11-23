package rw.qtglobal.diary.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rw.qtglobal.diary.entity.Mood;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.service.EntryService;
import rw.qtglobal.diary.security.CurrentUser;
import rw.qtglobal.diary.payload.EntryDto;
import rw.qtglobal.diary.payload.EntryRequest;
import rw.qtglobal.diary.payload.PageResponse;
import rw.qtglobal.diary.payload.StatsDto;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private final EntryService entries;

    public EntryController(EntryService entries) {
        this.entries = entries;
    }

    @GetMapping
    public PageResponse<EntryDto> list(@CurrentUser User user,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) Mood mood,
                                       @RequestParam(required = false) String tag,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return entries.list(user, q, mood, tag, from, to, page, size);
    }

    @GetMapping("/stats")
    public StatsDto stats(@CurrentUser User user) {
        return entries.stats(user);
    }

    @GetMapping("/{id}")
    public EntryDto get(@CurrentUser User user, @PathVariable Long id) {
        return entries.get(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntryDto create(@CurrentUser User user, @Valid @RequestBody EntryRequest req) {
        return entries.create(user, req);
    }

    @PutMapping("/{id}")
    public EntryDto update(@CurrentUser User user, @PathVariable Long id,
                           @Valid @RequestBody EntryRequest req) {
        return entries.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        entries.delete(user, id);
    }
}
