package pt.upt.studymatch.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.dto.AccountRequest;
import pt.upt.studymatch.dto.AccountResponse;
import pt.upt.studymatch.dto.LoginRequest;
import pt.upt.studymatch.dto.LoginResponse;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.exception.UnauthorizedException;
import pt.upt.studymatch.repository.UserAccountRepository;
import pt.upt.studymatch.security.CurrentUser;
import pt.upt.studymatch.security.TokenService;

/** Accounts and login (UC1, UC2). */
@Service
public class AccountService {

    private static final String INVALID_LOGIN = "Invalid username or password";

    private final UserAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUser currentUser;
    // Compared against when the username does not exist, so a wrong username takes as long as a
    // wrong password and the response time does not reveal which usernames exist.
    private final String unknownUserHash;

    public AccountService(UserAccountRepository accountRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService, CurrentUser currentUser) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.currentUser = currentUser;
        this.unknownUserHash = passwordEncoder.encode("not-a-real-password");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount account = accountRepository.findByUsername(normalise(request.username())).orElse(null);
        String hash = account == null ? unknownUserHash : account.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (account == null || !passwordMatches) {
            throw new UnauthorizedException(INVALID_LOGIN);
        }
        TokenService.IssuedToken token = tokenService.issue(account);
        return new LoginResponse(token.value(), token.expiresAt(), AccountResponse.from(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse me() {
        return AccountResponse.from(currentUser.require());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAllByOrderByUsername().stream().map(AccountResponse::from).toList();
    }

    @Transactional
    public AccountResponse register(AccountRequest request) {
        String username = normalise(request.username());
        if (accountRepository.existsByUsername(username)) {
            throw new ConflictException("Username " + username + " is already in use");
        }
        UserAccount account = new UserAccount(username, request.displayName().trim(),
                passwordEncoder.encode(request.password()), request.role());
        return AccountResponse.from(accountRepository.save(account));
    }

    private static String normalise(String username) {
        return username.trim().toLowerCase();
    }
}
