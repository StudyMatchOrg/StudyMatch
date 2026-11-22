package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/** Projects (grouping contexts) through the API, logged in as the teacher "marta". */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "marta", roles = "TEACHER")
class GroupingContextApiIntegrationTest {

    private static final String URL = "/api/grouping-contexts";

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
    private UserAccountRepository accountRepository;

    @Autowired
    private GroupingContextRepository contextRepository;

    private CourseUnit aed;
    private Competency programming;
    private Competency maths;
    private UserAccount rui;
    private final List<Student> students = new ArrayList<>();

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        aed = courseUnitRepository.save(new CourseUnit("AED", "Algoritmos e Estruturas de Dados", 7.5, 2, 1, Set.of(lei)));
        programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        maths = competencyRepository.save(new Competency("MATH", "Mathematical Reasoning", null));
        accountRepository.save(new UserAccount("marta", "Prof. Marta", "hash", Role.TEACHER));
        rui = accountRepository.save(new UserAccount("rui", "Prof. Rui", "hash", Role.TEACHER));
        accountRepository.save(new UserAccount("boss", "The Admin", "hash", Role.ADMIN));
        String[] names = {"Filipa", "Ana", "Eva", "Bruno", "Duarte", "Carla"};
        for (int i = 0; i < names.length; i++) {
            Student student = studentRepository.save(
                    new Student("5200" + i, names[i], "5200" + i + "@alunos.upt.pt", lei, 2025));
            students.add(student);
            if (i < 5) {
                recordRepository.save(new AcademicRecord(student, aed, "2026/2027", 1, RecordStatus.ENROLLED, null));
            }
        }
    }

    private String project(String name, int groupSize, List<Student> chosen, Map<Competency, Double> importance) {
        String studentIds = chosen.stream().map(s -> String.valueOf(s.getId())).collect(Collectors.joining(","));
        String competencies = importance.entrySet().stream()
                .map(e -> "{\"competencyId\":" + e.getKey().getId() + ",\"importance\":" + e.getValue() + "}")
                .collect(Collectors.joining(","));
        return """
                {"name": "%s", "description": "Practical project", "courseUnitId": %d, "academicYear": "2026/2027",
                 "groupSize": %d, "studentIds": [%s], "competencies": [%s]}
                """.formatted(name, aed.getId(), groupSize, studentIds, competencies);
    }

    private String sixStudentsInGroupsOfThree() {
        return project("AED project", 3, students, Map.of(programming, 1.0, maths, 0.5));
    }

    @Test
    void createProjectAndPreviewItsGroups() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(sixStudentsInGroupsOfThree()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AED project"))
                .andExpect(jsonPath("$.courseUnit.code").value("AED"))
                .andExpect(jsonPath("$.ownerName").value("Prof. Marta"))
                .andExpect(jsonPath("$.students.length()").value(6))
                .andExpect(jsonPath("$.students[0].name").value("Ana"))
                .andExpect(jsonPath("$.competencies[0].competency.code").value("PROG"))
                .andExpect(jsonPath("$.competencies[1].importance").value(0.5))
                .andExpect(jsonPath("$.plannedSizes.length()").value(2))
                .andExpect(jsonPath("$.plannedSizes[0]").value(3))
                .andExpect(jsonPath("$.warnings.length()").value(0));
    }

    @Test
    void candidatesAreTheStudentsEnrolledInTheCourseUnitThatYear() throws Exception {
        mockMvc.perform(get(URL + "/candidates").param("courseUnitId", aed.getId().toString())
                        .param("academicYear", "2026/2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[4].name").value("Filipa"));
    }

    @Test
    void aStudentChosenTwiceIsRejected() throws Exception {
        List<Student> twice = new ArrayList<>(students);
        twice.add(students.getFirst());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(project("AED project", 3, twice, Map.of(programming, 1.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Student 52000 is selected twice"));
    }

    @Test
    void invalidProjectsAreRejected() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(project("AED project", 1, students, Map.of(programming, 1.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Group size must be between 2 and 10, but was 1"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(project("AED project", 3, students, Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one competency must be relevant for the project"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(project("AED project", 3, students, Map.of(programming, 0.0))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupSize\": 3, \"studentIds\": [], \"competencies\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name must not be blank"));
    }

    @Test
    void anUnknownStudentIsReported() throws Exception {
        String body = project("AED project", 3, students, Map.of(programming, 1.0))
                .replace("\"studentIds\": [", "\"studentIds\": [999999,");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student 999999 does not exist"));
    }

    @Test
    void updatingTheGroupSizeChangesThePlannedGroups() throws Exception {
        String created = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(sixStudentsInGroupsOfThree()))
                .andReturn().getResponse().getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(project("AED project", 5, students, Map.of(programming, 1.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupSize").value(5))
                .andExpect(jsonPath("$.plannedSizes.length()").value(2))
                .andExpect(jsonPath("$.warnings[0]").value(
                        "6 students cannot all be placed in groups of 4 or 5, so the groups are 3 + 3."));
    }

    @Test
    void aTeacherCannotSeeAnotherTeachersProject() throws Exception {
        GroupingContext ruiProject = contextRepository.save(
                new GroupingContext("Rui's project", null, null, null, 4, rui, LocalDateTime.now()));

        mockMvc.perform(get(URL + "/" + ruiProject.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project " + ruiProject.getId() + " does not exist"));
        mockMvc.perform(get(URL)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "boss", roles = "ADMIN")
    void anAdminSeesEveryProject() throws Exception {
        GroupingContext ruiProject = contextRepository.save(
                new GroupingContext("Rui's project", null, null, null, 4, rui, LocalDateTime.now()));

        mockMvc.perform(get(URL)).andExpect(jsonPath("$[0].name").value("Rui's project"));
        mockMvc.perform(get(URL + "/" + ruiProject.getId())).andExpect(status().isOk());
    }
}
