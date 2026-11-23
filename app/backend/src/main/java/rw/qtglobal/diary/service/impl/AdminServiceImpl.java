package rw.qtglobal.diary.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.qtglobal.diary.config.CacheConfig;
import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.exception.ApiException;
import rw.qtglobal.diary.payload.AdminUserDto;
import rw.qtglobal.diary.payload.UserDto;
import rw.qtglobal.diary.repository.UserRepository;
import rw.qtglobal.diary.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository users;

    public AdminServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDto> listUsers() {
        return users.findAllWithEntryCount().stream()
                .map(row -> {
                    User u = (User) row[0];
                    return new AdminUserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole(),
                            u.getCreatedAt(), (Long) row[1]);
                })
                .toList();
    }

    @Override
    @Transactional
    public UserDto changeRole(User admin, Long id, Role role) {
        if (admin.getId().equals(id)) {
            throw ApiException.badRequest("you cannot change your own role");
        }
        User user = users.findById(id).orElseThrow(() -> ApiException.notFound("user not found"));
        user.setRole(role);
        return UserDto.from(users.save(user));
    }

    /** The user's entries are deleted by the database (ON DELETE CASCADE), so their cached data goes too. */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ENTRY_STATS, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ENTRIES, allEntries = true)})
    public void deleteUser(User admin, Long id) {
        if (admin.getId().equals(id)) {
            throw ApiException.badRequest("you cannot delete your own account");
        }
        if (!users.existsById(id)) {
            throw ApiException.notFound("user not found");
        }
        users.deleteById(id);
    }
}
