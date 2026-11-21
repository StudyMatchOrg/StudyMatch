package pt.upt.studymatch.dto;

import java.time.Instant;

/**
 * A successful login: the token to send as {@code Authorization: Bearer <token>} on every
 * request, when it expires, and who logged in.
 */
public record LoginResponse(String token, Instant expiresAt, AccountResponse account) {
}
