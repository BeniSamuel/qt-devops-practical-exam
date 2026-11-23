package rw.qtglobal.diary.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Injects the authenticated {@code User} entity into a controller method parameter:
 * {@code public EntryDto get(@CurrentUser User user, ...)}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "user")
public @interface CurrentUser {
}
