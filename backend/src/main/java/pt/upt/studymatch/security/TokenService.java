package pt.upt.studymatch.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pt.upt.studymatch.domain.UserAccount;

/** Issues the signed login tokens. They are checked by Spring Security on every request. */
@Service
public class TokenService {

    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final Duration validity;

    public TokenService(JwtEncoder encoder,
            @Value("${studymatch.security.token-validity:8h}") Duration validity) {
        this.encoder = encoder;
        this.validity = validity;
    }

    public IssuedToken issue(UserAccount account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(validity);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("studymatch")
                .subject(account.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("name", account.getDisplayName())
                .claim(ROLES_CLAIM, List.of(account.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
