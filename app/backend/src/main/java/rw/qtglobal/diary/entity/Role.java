package rw.qtglobal.diary.entity;

public enum Role {
    /** Writes and reads their own diary. */
    USER,
    /** Manages user accounts. Admins cannot read other people's diary entries. */
    ADMIN
}
