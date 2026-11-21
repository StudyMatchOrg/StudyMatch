package pt.upt.studymatch.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Answers 401 (not logged in) and 403 (logged in, but not allowed) with the same JSON as every
 * other API error, so the frontend can show the message.
 */
@Component
public class JsonSecurityErrors implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        boolean sentToken = request.getHeader(HttpHeaders.AUTHORIZATION) != null;
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        write(response, HttpStatus.UNAUTHORIZED, sentToken
                ? "Your login is invalid or has expired. Log in again."
                : "You need to log in to use StudyMatch.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "Your account does not have permission for this action.");
    }

    // The messages are fixed texts without quotes, so they can be written without a JSON library.
    private static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase()
                + "\",\"message\":\"" + message + "\"}");
    }
}
