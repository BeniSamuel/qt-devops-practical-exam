package rw.qtglobal.diary.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import rw.qtglobal.diary.entity.User;

/** Adapts the {@link User} entity to Spring Security's {@link UserDetails}. */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    /** The underlying entity, exposed to controllers through {@link CurrentUser}. */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
