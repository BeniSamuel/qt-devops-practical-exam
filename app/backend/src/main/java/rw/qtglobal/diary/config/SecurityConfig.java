package rw.qtglobal.diary.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import rw.qtglobal.diary.security.JsonAccessDeniedHandler;
import rw.qtglobal.diary.security.JwtAuthenticationEntryPoint;
import rw.qtglobal.diary.security.JwtAuthenticationFilter;

/**
 * Stateless JWT security:
 * <ul>
 *   <li>Login: {@code AuthenticationManager} → {@code DaoAuthenticationProvider} →
 *       {@code CustomUserDetailsService} + {@code BCryptPasswordEncoder}, then a JWT is issued.</li>
 *   <li>Requests: {@link JwtAuthenticationFilter} validates the Bearer token and populates the SecurityContext.</li>
 *   <li>Authorization: URL rules below plus method security ({@code @PreAuthorize}).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_POST = {"/api/auth/register", "/api/auth/login"};
    private static final String[] PUBLIC_GET = {"/api/health", "/actuator/health/**", "/actuator/info"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)          // stateless API, no cookies
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Built by Spring from the single UserDetailsService and PasswordEncoder beans (DaoAuthenticationProvider). */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** The JWT filter runs inside the security chain only, not a second time as a plain servlet filter. */
    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(props.cors().allowedOrigins());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
