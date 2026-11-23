package rw.qtglobal.diary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import rw.qtglobal.diary.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Returns rows of [User, entryCount]. */
    @Query("select u, count(e) from User u left join DiaryEntry e on e.user = u group by u order by u.createdAt")
    List<Object[]> findAllWithEntryCount();
}
