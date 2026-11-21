package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * An account that can log in. Only admins and teachers have accounts; students are managed by
 * them and do not log in. The password is never stored, only its BCrypt hash.
 */
@Entity
public class UserAccount {

    private static final Pattern USERNAME = Pattern.compile("[a-z0-9._-]{3,30}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    protected UserAccount() {
        // required by JPA
    }

    public UserAccount(String username, String displayName, String passwordHash, Role role) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new BusinessRuleException(
                    "Username must have 3 to 30 characters: lowercase letters, digits, '.', '_' or '-'");
        }
        if (role == null) {
            throw new BusinessRuleException("Role is required");
        }
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
