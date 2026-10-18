package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.repository.DegreeRepository;

// End-to-end test of the student API, from HTTP request to in-memory DB.
// Each test is rolled back afterwards.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    private Degree lei;

    @BeforeEach
    void createDegree() {
        lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
    }

    private String miguel() {
        return """
                {"studentNumber": "51631", "name": "Miguel Leitão", "email": "51631@alunos.upt.pt",
                 "degreeId": %d, "enrolmentYear": 2025}
                """.formatted(lei.getId());
    }

    private String postMiguel() throws Exception {
        String body = mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(miguel()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id").toString();
    }

    @Test
    void registeredStudentIsReturnedInTheListAndById() throws Exception {
        String id = postMiguel();

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].degree.code").value("LEI"));
        mockMvc.perform(get("/api/students/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Miguel Leitão"))
                .andExpect(jsonPath("$.enrolmentYear").value(2025));
    }

    @Test
    void studentCanBeUpdated() throws Exception {
        String id = postMiguel();
        String updated = miguel().replace("Miguel Leitão", "Miguel M. Leitão").replace("2025", "2024");

        mockMvc.perform(put("/api/students/" + id).contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Miguel M. Leitão"))
                .andExpect(jsonPath("$.enrolmentYear").value(2024));
    }

    @Test
    void unknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/students/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student 9999 does not exist"));
        mockMvc.perform(put("/api/students/9999").contentType(MediaType.APPLICATION_JSON).content(miguel()))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateStudentNumberReturnsConflict() throws Exception {
        postMiguel();

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(miguel()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void invalidStudentReturnsBadRequest() throws Exception {
        String invalid = """
                {"studentNumber": "", "name": "Miguel Leitão", "email": "not-an-email", "enrolmentYear": 2025}
                """;

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("email")))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("studentNumber")))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("degreeId")));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }
}
