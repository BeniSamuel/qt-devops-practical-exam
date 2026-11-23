package rw.qtglobal.diary.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import rw.qtglobal.diary.entity.Role;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.repository.UserRepository;

class CustomUserDetailsServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final CustomUserDetailsService service = new CustomUserDetailsService(users);

    @Test
    void loadsUserByEmailWithRoleAuthority() {
        User admin = new User("Alice", "admin@qt.rw", "$2a$hash", Role.ADMIN);
        when(users.findByEmailIgnoreCase("admin@qt.rw")).thenReturn(Optional.of(admin));

        UserPrincipal principal = service.loadUserByUsername(" admin@qt.rw ");

        assertThat(principal.getUsername()).isEqualTo("admin@qt.rw");
        assertThat(principal.getPassword()).isEqualTo("$2a$hash");
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getUser()).isSameAs(admin);
    }

    @Test
    void unknownEmailThrowsUsernameNotFound() {
        when(users.findByEmailIgnoreCase("nobody@qt.rw")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("nobody@qt.rw"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void unknownIdThrowsUsernameNotFound() {
        when(users.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserById(99L)).isInstanceOf(UsernameNotFoundException.class);
    }
}
