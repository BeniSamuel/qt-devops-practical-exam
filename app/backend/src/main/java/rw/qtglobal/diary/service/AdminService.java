package rw.qtglobal.diary.service;

import java.util.List;

import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.payload.AdminUserDto;
import rw.qtglobal.diary.payload.UserDto;

/** Account management. Admins see how many entries a user has, never the entries themselves. */
public interface AdminService {

    List<AdminUserDto> listUsers();

    UserDto changeRole(User admin, Long id, Role role);

    /** Deleting a user also deletes their diary entries (ON DELETE CASCADE). */
    void deleteUser(User admin, Long id);
}
