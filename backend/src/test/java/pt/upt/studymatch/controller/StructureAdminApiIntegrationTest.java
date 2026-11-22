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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.repository.DegreeRepository;

/** Admins create degrees and course units (UC3, UC5). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class StructureAdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    private Degree lei;

    @BeforeEach
    void createDegree() {
        lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
    }

    private String courseUnit(String code, double ects, int semester) {
        return """
                {"code": "%s", "name": "Engenharia de Requisitos", "ects": %s, "curricularYear": 3,
                 "semester": %d, "degreeIds": [%d]}
                """.formatted(code, ects, semester, lei.getId());
    }

    @Test
    void createADegree() throws Exception {
        mockMvc.perform(post("/api/degrees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"lcd\", \"name\": \"Licenciatura em Ciência de Dados\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("LCD"));
        mockMvc.perform(get("/api/degrees")).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void degreeAndCourseUnitCodesAreUnique() throws Exception {
        mockMvc.perform(post("/api/degrees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"LEI\", \"name\": \"Another\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Degree code LEI already exists"));
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON).content(courseUnit("ER", 6, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.degreeCodes[0]").value("LEI"));
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON).content(courseUnit("er", 6, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Course unit code ER already exists"));
    }

    @Test
    void courseUnitRulesAreChecked() throws Exception {
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON).content(courseUnit("ER", 0, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ECTS must be greater than 0"));
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON).content(courseUnit("ER", 6, 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Semester must be 1 or 2"));
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON)
                        .content(courseUnit("ER", 6, 1).replace("\"degreeIds\": [" + lei.getId() + "]", "\"degreeIds\": []")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A course unit must belong to at least one degree"));
        mockMvc.perform(post("/api/course-units").contentType(MediaType.APPLICATION_JSON)
                        .content(courseUnit("ER", 6, 1).replace("[" + lei.getId() + "]", "[9999]")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teachersCannotChangeTheAcademicStructure() throws Exception {
        mockMvc.perform(post("/api/degrees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"LCD\", \"name\": \"Ciência de Dados\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/degrees")).andExpect(status().isOk());
    }
}
