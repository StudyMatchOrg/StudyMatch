package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssessmentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    private Student bruno;
    private Competency programming;
    private Competency mathematics;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        bruno = studentRepository.save(new Student("53007", "Bruno Costa", "53007@alunos.upt.pt", lei, 2026));
        programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        mathematics = competencyRepository.save(new Competency("MATH", "Mathematical Reasoning", null));
    }

    private String url() {
        return "/api/students/" + bruno.getId() + "/assessments";
    }

    private String body(Long competencyId, Double value, Long otherId, Double otherValue) {
        return """
                {"assessments": [{"competencyId": %d, "value": %s}, {"competencyId": %d, "value": %s}]}
                """.formatted(competencyId, value, otherId, otherValue);
    }

    @Test
    void assessmentsAreSavedAndListedByCompetencyName() throws Exception {
        mockMvc.perform(put(url()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(programming.getId(), 0.6, mathematics.getId(), 0.7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get(url()))
                .andExpect(jsonPath("$[0].competency.code").value("MATH"))
                .andExpect(jsonPath("$[0].value").value(0.7))
                .andExpect(jsonPath("$[1].competency.code").value("PROG"));
    }

    @Test
    void nullValueRemovesAnAssessment() throws Exception {
        mockMvc.perform(put(url()).contentType(MediaType.APPLICATION_JSON)
                .content(body(programming.getId(), 0.6, mathematics.getId(), 0.7)));

        mockMvc.perform(put(url()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(programming.getId(), 0.8, mathematics.getId(), null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].value").value(0.8));
    }

    @Test
    void valueAboveOneIsRejected() throws Exception {
        mockMvc.perform(put(url()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(programming.getId(), 1.5, mathematics.getId(), 0.7)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Assessment value must be between 0 and 1, but was 1.5"));
    }

    @Test
    void sameCompetencyTwiceIsRejected() throws Exception {
        mockMvc.perform(put(url()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(programming.getId(), 0.6, programming.getId(), 0.7)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Competency " + programming.getId() + " is assessed more than once"));
    }

    @Test
    void unknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/students/9999/assessments"))
                .andExpect(status().isNotFound());
    }
}
