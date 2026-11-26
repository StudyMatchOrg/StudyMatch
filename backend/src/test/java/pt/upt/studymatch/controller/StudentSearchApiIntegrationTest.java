package pt.upt.studymatch.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

/** Student search and filters (UC16). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "TEACHER")
class StudentSearchApiIntegrationTest {

    private static final String URL = "/api/students";

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

    @Autowired
    private CompetencyAssessmentRepository assessmentRepository;

    private Degree lig;
    private CourseUnit aed;

    @BeforeEach
    void createStudents() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        lig = degreeRepository.save(new Degree("LIG", "Informática de Gestão"));
        CourseUnit ip = courseUnitRepository.save(
                new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei, lig)));
        aed = courseUnitRepository.save(new CourseUnit("AED", "Algoritmos e Estruturas de Dados", 7.5, 2, 1, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));

        Student joao = studentRepository.save(new Student("52040", "João Ribeiro", "52040@upt.pt", lei, 2025));
        Student ines = studentRepository.save(new Student("52043", "Inês Carvalho", "52043@upt.pt", lei, 2025));
        Student pedro = studentRepository.save(new Student("52044", "Pedro Gomes", "52044@upt.pt", lig, 2025));
        Student lara = studentRepository.save(new Student("52059", "Lara Azevedo", "52059@upt.pt", lei, 2024));
        record(joao, ip, "2025/2026", RecordStatus.PASSED, 18);
        record(ines, ip, "2025/2026", RecordStatus.PASSED, 12);
        record(pedro, ip, "2025/2026", RecordStatus.PASSED, 10);
        record(joao, aed, "2026/2027", RecordStatus.ENROLLED, null);
        record(lara, aed, "2026/2027", RecordStatus.ENROLLED, null);
        assessmentRepository.save(new CompetencyAssessment(lara, programming, 0.8, LocalDate.of(2026, 10, 16)));
    }

    private void record(Student student, CourseUnit unit, String year, RecordStatus status, Integer grade) {
        recordRepository.save(new AcademicRecord(student, unit, year, 1, status, grade));
    }

    @Test
    void withoutFiltersEveryStudentIsListedByNumber() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].studentNumber").value("52040"))
                .andExpect(jsonPath("$[0].competencyScore").doesNotExist());
    }

    @Test
    void nameSearchIgnoresCaseAndAccents() throws Exception {
        mockMvc.perform(get(URL).param("q", "joao"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("João Ribeiro"));
        mockMvc.perform(get(URL).param("q", "INES"))
                .andExpect(jsonPath("$[0].name").value("Inês Carvalho"));
        mockMvc.perform(get(URL).param("q", "5205"))
                .andExpect(jsonPath("$[0].name").value("Lara Azevedo"));
    }

    @Test
    void filtersCombine() throws Exception {
        mockMvc.perform(get(URL).param("degreeId", lig.getId().toString()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Pedro Gomes"));
        mockMvc.perform(get(URL).param("enrolmentYear", "2025").param("courseUnitId", aed.getId().toString()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("João Ribeiro"));
    }

    @Test
    void minimumScoreInACompetencyAndSortingByScore() throws Exception {
        mockMvc.perform(get(URL).param("competency", "PROG").param("minScore", "0.6").param("sort", "score"))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("João Ribeiro"))
                .andExpect(jsonPath("$[0].competencyScore").value(closeTo(0.9, 0.001)))
                .andExpect(jsonPath("$[1].name").value("Lara Azevedo"))
                .andExpect(jsonPath("$[2].name").value("Inês Carvalho"));
    }

    @Test
    void lowEvidenceFindsStudentsWithoutGrades() throws Exception {
        mockMvc.perform(get(URL).param("lowEvidence", "true"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Lara Azevedo"))
                .andExpect(jsonPath("$[0].lowEvidence").value(true));
    }

    @Test
    void invalidFiltersAreRejected() throws Exception {
        mockMvc.perform(get(URL).param("minScore", "0.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choose a competency to filter or sort by score"));
        mockMvc.perform(get(URL).param("competency", "PROG").param("minScore", "1.5"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(URL).param("sort", "age"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(URL).param("competency", "NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noResultsGiveAnEmptyList() throws Exception {
        mockMvc.perform(get(URL).param("q", "nobody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
