package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * End-to-end test of the backend: HTTP request, service, JPA and an in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentApiIntegrationTest {

    private static final String MIGUEL = """
            {"studentNumber": "51631", "name": "Miguel Leitão", "email": "51631@alunos.upt.pt"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDatabase() {
        studentRepository.deleteAll();
    }

    @Test
    void registeredStudentIsReturnedInTheList() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(MIGUEL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.studentNumber").value("51631"));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Miguel Leitão"));
    }

    @Test
    void duplicateStudentNumberReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(MIGUEL))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(MIGUEL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void invalidStudentReturnsBadRequest() throws Exception {
        String invalid = """
                {"studentNumber": "", "name": "Miguel Leitão", "email": "not-an-email"}
                """;

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("studentNumber")));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }
}
