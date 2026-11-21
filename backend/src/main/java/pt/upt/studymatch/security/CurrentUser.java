package pt.upt.studymatch.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.exception.UnauthorizedException;
import pt.upt.studymatch.repository.UserAccountRepository;

/** The account that made the current request. */
@Component
public class CurrentUser {

    private final UserAccountRepository accountRepository;

    public CurrentUser(UserAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UserAccount require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("You need to log in to use StudyMatch.");
        }
        return accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Account " + authentication.getName() + " no longer exists"));
    }
}
