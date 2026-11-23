package rw.qtglobal.diary.service;

import java.util.Map;

public interface SeedService {

    /** Idempotent: only creates users that do not exist and sample entries for users that have none. */
    Map<String, Object> seed();
}
