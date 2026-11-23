package rw.qtglobal.diary.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "diary_entries")
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mood mood;

    /** Comma-separated, lower-case tags. */
    @Column(length = 500)
    private String tags;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected DiaryEntry() {
    }

    public DiaryEntry(User user) {
        this.user = user;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Mood getMood() { return mood; }
    public String getTags() { return tags; }
    public LocalDate getEntryDate() { return entryDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setMood(Mood mood) { this.mood = mood; }
    public void setTags(String tags) { this.tags = tags; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}
