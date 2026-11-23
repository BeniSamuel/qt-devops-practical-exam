package rw.qtglobal.diary.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> handleApi(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation failed");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "malformed JSON or invalid field value");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "invalid value for parameter '" + e.getName() + "'");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "route not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> handleMethod(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    /** Failed login (bad email or password). The message never says which one was wrong. */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException e) {
        return error(HttpStatus.UNAUTHORIZED, "invalid email or password");
    }

    /** Thrown by method security (@PreAuthorize). */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "your role is not allowed to perform this action");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        log.error("Unhandled error", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
