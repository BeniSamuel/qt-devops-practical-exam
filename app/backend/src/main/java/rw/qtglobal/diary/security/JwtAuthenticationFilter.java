package rw.qtglobal.diary.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates requests carrying "Authorization: Bearer &lt;jwt&gt;". The user is reloaded from the
 * database on every request, so deleted accounts and role changes take effect immediately.
 * Requests without a valid token continue unauthenticated; the authorization rules then reject them
 * through {@link JwtAuthenticationEntryPoint}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.parseUserId(header.substring(BEARER_PREFIX.length())).ifPresent(userId -> {
                try {
                    UserPrincipal principal = userDetailsService.loadUserById(userId);
                    var authentication = UsernamePasswordAuthenticationToken.authenticated(
                            principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                } catch (UsernameNotFoundException e) {
                    // Token belongs to a deleted user: leave the request unauthenticated.
                }
            });
        }
        chain.doFilter(request, response);
    }
}
