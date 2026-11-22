package pt.upt.studymatch.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

/** Teachers configure competencies and course-unit weights (UC11). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "TEACHER")
class CompetencyEditingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CourseUnitCompetencyRepository linkRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicRecordRepository recordRepository;

    private CourseUnit ip;
    private CourseUnit md;
    private Competency programming;
    private CourseUnitCompetency ipProgramming;
    private Student ana;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        md = courseUnitRepository.save(new CourseUnit("MD", "Matemática Discreta", 6, 1, 1, Set.of(lei)));
        programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        ipProgramming = linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));
        ana = studentRepository.save(new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", lei, 2025));
        recordRepository.save(new AcademicRecord(ana, ip, "2025/2026", 1, RecordStatus.PASSED, 17));
        recordRepository.save(new AcademicRecord(ana, md, "2025/2026", 1, RecordStatus.PASSED, 10));
    }

    @Test
    void createAndRenameACompetency() throws Exception {
        String created = mockMvc.perform(post("/api/competencies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"test\", \"name\": \"Testing\", \"description\": \"Unit tests\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");

        mockMvc.perform(put("/api/competencies/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Software Testing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Software Testing"))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void competencyCodesAreUniqueAndWellFormed() throws Exception {
        mockMvc.perform(post("/api/competencies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"PROG\", \"name\": \"Programming again\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Competency code PROG already exists"));
        mockMvc.perform(post("/api/competencies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"A B\", \"name\": \"Spaces\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linkingACourseUnitChangesTheProfile() throws Exception {
        mockMvc.perform(get("/api/students/" + ana.getId() + "/profile"))
                .andExpect(jsonPath("$.competencies[0].score").value(closeTo(0.85, 0.001)));

        mockMvc.perform(post("/api/course-unit-competencies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseUnitId\": %d, \"competencyId\": %d, \"weight\": 1.0}"
                                .formatted(md.getId(), programming.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseUnitCode").value("MD"))
                .andExpect(jsonPath("$.id").isNumber());

        // (17/20 + 10/20) / 2
        mockMvc.perform(get("/api/students/" + ana.getId() + "/profile"))
                .andExpect(jsonPath("$.competencies[0].score").value(closeTo(0.675, 0.001)));
    }

    @Test
    void theSameLinkTwiceIsAConflict() throws Exception {
        mockMvc.perform(post("/api/course-unit-competencies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseUnitId\": %d, \"competencyId\": %d, \"weight\": 0.5}"
                                .formatted(ip.getId(), programming.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("IP is already linked to Programming"));
    }

    @Test
    void changeAWeightAndRemoveALink() throws Exception {
        mockMvc.perform(put("/api/course-unit-competencies/" + ipProgramming.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"weight\": 0.4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(0.4));
        mockMvc.perform(put("/api/course-unit-competencies/" + ipProgramming.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"weight\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Competency weight must be greater than 0 and at most 1, but was 0.0"));

        mockMvc.perform(delete("/api/course-unit-competencies/" + ipProgramming.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/students/" + ana.getId() + "/profile"))
                .andExpect(jsonPath("$.competencies[0].status").value("UNKNOWN"));
        mockMvc.perform(delete("/api/course-unit-competencies/" + ipProgramming.getId()))
                .andExpect(status().isNotFound());
    }
}
