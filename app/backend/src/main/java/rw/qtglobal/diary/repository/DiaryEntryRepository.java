package rw.qtglobal.diary.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rw.qtglobal.diary.entity.DiaryEntry;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long>, JpaSpecificationExecutor<DiaryEntry> {

    Optional<DiaryEntry> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndEntryDateBetween(Long userId, LocalDate from, LocalDate to);

    @Query("select distinct e.entryDate from DiaryEntry e where e.user.id = :userId order by e.entryDate desc")
    List<LocalDate> findEntryDates(@Param("userId") Long userId);

    /** Returns rows of [Mood, count]. */
    @Query("select e.mood, count(e) from DiaryEntry e where e.user.id = :userId group by e.mood")
    List<Object[]> countByMood(@Param("userId") Long userId);

    @Query("select e.tags from DiaryEntry e where e.user.id = :userId and e.tags is not null")
    List<String> findTags(@Param("userId") Long userId);
}
