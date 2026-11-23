package rw.qtglobal.diary.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Returns 403 JSON when an authenticated user lacks the required role. */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public JsonAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), Map.of("error", "your role is not allowed to perform this action"));
    }
}
