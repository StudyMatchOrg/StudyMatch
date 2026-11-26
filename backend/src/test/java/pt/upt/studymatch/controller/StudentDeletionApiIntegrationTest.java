package pt.upt.studymatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/** Deleting students (UC9), as an admin. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "boss", roles = "ADMIN")
class StudentDeletionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicRecordRepository recordRepository;

    @Autowired
    private CompetencyAssessmentRepository assessmentRepository;

    @Autowired
    private UserAccountRepository accountRepository;

    @Autowired
    private GroupingContextRepository contextRepository;

    private Student ana;
    private Student bruno;
    private Student carla;
    private GroupingContext project;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        CourseUnit ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        UserAccount boss = accountRepository.save(new UserAccount("boss", "The Admin", "hash", Role.ADMIN));
        ana = studentRepository.save(new Student("52031", "Ana Sousa", "52031@upt.pt", lei, 2025));
        bruno = studentRepository.save(new Student("53007", "Bruno Costa", "53007@upt.pt", lei, 2026));
        carla = studentRepository.save(new Student("53015", "Carla Mendes", "53015@upt.pt", lei, 2026));
        recordRepository.save(new AcademicRecord(bruno, ip, "2026/2027", 1, RecordStatus.ENROLLED, null));
        assessmentRepository.save(new CompetencyAssessment(bruno, programming, 0.6, LocalDate.of(2026, 10, 9)));
        project = new GroupingContext("IP project", null, null, null, 2, boss, LocalDateTime.now());
        project.replaceStudents(List.of(ana, bruno, carla));
        project.replaceCompetencies(Map.of(programming, 1.0));
        project = contextRepository.save(project);
    }

    @Test
    void deletingAStudentRemovesTheirRecordsAssessmentsAndProjectMemberships() throws Exception {
        mockMvc.perform(delete("/api/students/" + bruno.getId())).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/" + bruno.getId())).andExpect(status().isNotFound());
        assertThat(recordRepository.existsByStudentId(bruno.getId())).isFalse();
        assertThat(assessmentRepository.existsByStudentId(bruno.getId())).isFalse();
        mockMvc.perform(get("/api/grouping-contexts/" + project.getId()))
                .andExpect(jsonPath("$.students.length()").value(2));
    }

    @Test
    void aStudentInSavedGroupsCannotBeDeleted() throws Exception {
        mockMvc.perform(post("/api/grouping-contexts/" + project.getId() + "/group-sets")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/students/" + ana.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Student 52031 is in saved groups and cannot be deleted, so that earlier groups stay complete"));
    }

    @Test
    void onlyAdminsDeleteStudents() throws Exception {
        mockMvc.perform(delete("/api/students/" + carla.getId()).with(user("marta").roles("TEACHER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/students/999999")).andExpect(status().isNotFound());
    }
}
