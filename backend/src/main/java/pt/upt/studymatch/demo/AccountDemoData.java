package pt.upt.studymatch.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.UserAccountRepository;

/**
 * Demo accounts, so the application can be used right after starting it. These passwords are
 * demo values, documented in the README; change them, or start without demo data
 * ({@code DEMO_DATA=false}), outside development. Each account is only added if missing.
 */
@Component
@Order(5)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class AccountDemoData implements ApplicationRunner {

    private final UserAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountDemoData(UserAccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        account("admin", "Administrator", "admin123", Role.ADMIN);
        account("teacher", "Prof. Marta Lopes", "teacher123", Role.TEACHER);
        account("teacher2", "Prof. Rui Almeida", "teacher123", Role.TEACHER);
    }

    private void account(String username, String displayName, String password, Role role) {
        if (!accountRepository.existsByUsername(username)) {
            accountRepository.save(new UserAccount(username, displayName, passwordEncoder.encode(password), role));
        }
    }
}
