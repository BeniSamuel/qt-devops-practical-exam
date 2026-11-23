package rw.qtglobal.diary.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.qtglobal.diary.repository.UserRepository;

/**
 * Loads users for Spring Security. Used by the {@code DaoAuthenticationProvider} during login (by email)
 * and by {@link JwtAuthenticationFilter} on every authenticated request (by id).
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserPrincipal loadUserByUsername(String email) {
        return users.findByEmailIgnoreCase(email.trim())
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }

    public UserPrincipal loadUserById(Long id) {
        return users.findById(id)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
