package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.UserAccountRepository;

/** Login with real tokens, and which role may call what. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAccounts() {
        accountRepository.save(new UserAccount("boss", "The Admin", passwordEncoder.encode("admin-pass"), Role.ADMIN));
        accountRepository.save(new UserAccount("marta", "Prof. Marta", passwordEncoder.encode("teacher-pass"), Role.TEACHER));
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    @Test
    void loginReturnsATokenThatIdentifiesTheAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Marta \",\"password\":\"teacher-pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.account.username").value("marta"))
                .andExpect(jsonPath("$.account.role").value("TEACHER"))
                .andExpect(jsonPath("$.account.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, login("marta", "teacher-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Prof. Marta"));
    }

    @Test
    void wrongPasswordAndUnknownUserGiveTheSameAnswer() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"marta\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void theApiNeedsALogin() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.message").value("You need to log in to use StudyMatch."));
        mockMvc.perform(get("/api/students").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your login is invalid or has expired. Log in again."));
    }

    @Test
    void theHealthCheckIsPublic() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void aTeacherCanReadButNotUseAdminFeatures() throws Exception {
        String teacher = login("marta", "teacher-pass");

        mockMvc.perform(get("/api/students").header(HttpHeaders.AUTHORIZATION, teacher))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/accounts").header(HttpHeaders.AUTHORIZATION, teacher))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Your account does not have permission for this action."));
        mockMvc.perform(post("/api/students").header(HttpHeaders.AUTHORIZATION, teacher)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAdminCreatesAccountsWithUniqueUsernames() throws Exception {
        String admin = login("boss", "admin-pass");
        String newTeacher = "{\"username\":\"rui\",\"displayName\":\"Prof. Rui\",\"password\":\"long-enough\",\"role\":\"TEACHER\"}";

        mockMvc.perform(post("/api/accounts").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(newTeacher))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("rui"));
        mockMvc.perform(post("/api/accounts").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(newTeacher))
                .andExpect(status().isConflict());
        login("rui", "long-enough");
    }

    @Test
    void passwordsNeedAtLeastEightCharacters() throws Exception {
        mockMvc.perform(post("/api/accounts").header(HttpHeaders.AUTHORIZATION, login("boss", "admin-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rui\",\"displayName\":\"Rui\",\"password\":\"short\",\"role\":\"TEACHER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password size must be between 8 and 72"));
    }
}
