package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.UserAccount;

/** An account as the API shows it. The password hash is never sent. */
public record AccountResponse(Long id, String username, String displayName, Role role) {

    public static AccountResponse from(UserAccount account) {
        return new AccountResponse(account.getId(), account.getUsername(), account.getDisplayName(), account.getRole());
    }
}
