package pt.upt.studymatch.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileApiIntegrationTest {

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

    private Student ana;
    private CourseUnit poo;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        CourseUnit ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        poo = courseUnitRepository.save(new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        competencyRepository.save(new Competency("NET", "Networking", null));
        linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));
        linkRepository.save(new CourseUnitCompetency(poo, programming, 1.0));
        ana = studentRepository.save(new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", lei, 2025));
        recordRepository.save(new AcademicRecord(ana, ip, "2025/2026", 1, RecordStatus.PASSED, 17));
    }

    private String profileUrl() {
        return "/api/students/" + ana.getId() + "/profile";
    }

    @Test
    void profileExplainsEachCompetency() throws Exception {
        mockMvc.perform(get(profileUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Ana Sousa"))
                .andExpect(jsonPath("$.rules.confidenceConstant").value(2.0))
                .andExpect(jsonPath("$.competencies.length()").value(2))
                .andExpect(jsonPath("$.competencies[0].competency.code").value("NET"))
                .andExpect(jsonPath("$.competencies[0].status").value("UNKNOWN"))
                .andExpect(jsonPath("$.competencies[0].score").doesNotExist())
                .andExpect(jsonPath("$.competencies[1].status").value("ACADEMIC"))
                .andExpect(jsonPath("$.competencies[1].score").value(closeTo(0.85, 0.001)))
                .andExpect(jsonPath("$.competencies[1].evidence[0].type").value("ACADEMIC_RESULT"))
                .andExpect(jsonPath("$.competencies[1].evidence[0].share").value(1.0));
    }

    @Test
    void profileReflectsANewlyAddedGrade() throws Exception {
        String record = """
                {"courseUnitId": %d, "academicYear": "2025/2026", "semester": 2, "status": "PASSED", "grade": 11}
                """.formatted(poo.getId());
        mockMvc.perform(post("/api/students/" + ana.getId() + "/academic-records")
                        .contentType(MediaType.APPLICATION_JSON).content(record))
                .andExpect(status().isCreated());

        // (0.85 × 1.0 + 0.55 × 1.25) / 2.25
        mockMvc.perform(get(profileUrl()))
                .andExpect(jsonPath("$.competencies[1].score").value(closeTo(0.683, 0.001)))
                .andExpect(jsonPath("$.competencies[1].evidence.length()").value(2));
    }

    @Test
    void profileOfUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/students/9999/profile"))
                .andExpect(status().isNotFound());
    }
}
