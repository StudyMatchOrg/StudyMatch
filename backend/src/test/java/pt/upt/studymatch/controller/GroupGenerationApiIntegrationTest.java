package pt.upt.studymatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Role;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/** Generating, saving and consulting groups through the API, as the teacher "marta". */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "marta", roles = "TEACHER")
class GroupGenerationApiIntegrationTest {

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
    private UserAccountRepository accountRepository;

    @Autowired
    private GroupingContextRepository contextRepository;

    private GroupingContext project;
    private CourseUnit ip;
    private final List<Student> students = new ArrayList<>();

    @BeforeEach
    void createProjectWithElevenStudents() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        CourseUnit md = courseUnitRepository.save(new CourseUnit("MD", "Matemática Discreta", 6, 1, 1, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", null));
        Competency maths = competencyRepository.save(new Competency("MATH", "Mathematical Reasoning", null));
        linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));
        linkRepository.save(new CourseUnitCompetency(md, maths, 1.0));
        UserAccount marta = accountRepository.save(new UserAccount("marta", "Prof. Marta", "hash", Role.TEACHER));
        int[][] grades = {{18, 9}, {12, 18}, {15, 14}, {19, 16}, {10, 10}, {13, 17}, {17, 11}, {11, 19}, {14, 12}};
        for (int i = 0; i < 11; i++) {
            Student student = studentRepository.save(
                    new Student("520" + (10 + i), "Student " + (char) ('A' + i), "520" + (10 + i) + "@upt.pt", lei, 2025));
            students.add(student);
            if (i < grades.length) {
                grade(student, ip, grades[i][0]);
                grade(student, md, grades[i][1]);
            }
        }
        project = new GroupingContext("IP project", null, null, null, 5, marta, LocalDateTime.now());
        project.replaceStudents(students);
        Map<Competency, Double> importance = new LinkedHashMap<>();
        importance.put(programming, 1.0);
        importance.put(maths, 0.5);
        project.replaceCompetencies(importance);
        project = contextRepository.save(project);
    }

    private void grade(Student student, CourseUnit unit, int grade) {
        recordRepository.save(new AcademicRecord(student, unit, "2025/2026", 1,
                grade >= 10 ? RecordStatus.PASSED : RecordStatus.FAILED, grade));
    }

    private String url() {
        return "/api/grouping-contexts/" + project.getId() + "/group-sets";
    }

    private String generate(String body) throws Exception {
        return mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void generatedGroupsPlaceEveryStudentOnceAndAreExplained() throws Exception {
        String result = generate("{\"strategy\": \"BALANCED\"}");

        assertThat(JsonPath.<List<Integer>>read(result, "$.groups[*].members.length()")).containsExactly(4, 4, 3);
        assertThat(JsonPath.<List<String>>read(result, "$.groups[*].members[*].studentNumber"))
                .doesNotHaveDuplicates().hasSize(11);
        assertThat(JsonPath.<String>read(result, "$.strategy")).isEqualTo("BALANCED");
        assertThat(JsonPath.<String>read(result, "$.projectName")).isEqualTo("IP project");
        assertThat(JsonPath.<String>read(result, "$.generatedBy")).isEqualTo("Prof. Marta");
        assertThat(JsonPath.<Integer>read(result, "$.groupSize")).isEqualTo(5);
        assertThat(JsonPath.<String>read(result, "$.warnings[0]")).contains("11 students");
        assertThat(JsonPath.<List<String>>read(result, "$.groups[0].competencies[*].name"))
                .containsExactly("Programming", "Mathematical Reasoning");
        assertThat(JsonPath.<Double>read(result, "$.weakestScore"))
                .isGreaterThanOrEqualTo(JsonPath.<Double>read(result, "$.randomWeakestScore"));
    }

    @Test
    void studentsWithoutGradesAreGroupedAndFlaggedAsLowEvidence() throws Exception {
        String result = generate("{}");

        assertThat(JsonPath.<List<String>>read(result, "$.groups[*].members[?(@.lowEvidence == true)].studentNumber"))
                .containsExactlyInAnyOrder("52019", "52020");
    }

    @Test
    void everyGenerationIsSavedAndCanBeConsultedLater() throws Exception {
        String first = generate("{\"strategy\": \"BALANCED\"}");
        Number firstId = JsonPath.read(first, "$.id");
        grade(students.get(10), ip, 20);
        generate("{\"strategy\": \"RANDOM\", \"seed\": 7}");

        mockMvc.perform(get(url()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].strategy").value("RANDOM"))
                .andExpect(jsonPath("$[1].strategy").value("BALANCED"))
                .andExpect(jsonPath("$[1].studentCount").value(11))
                .andExpect(jsonPath("$[1].groupCount").value(3));
        mockMvc.perform(get("/api/group-sets/" + firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.length()").value(3))
                .andExpect(jsonPath("$.groups[0].number").value(1));
    }

    @Test
    void theSameSeedGivesTheSameGroups() throws Exception {
        String first = generate("{\"strategy\": \"RANDOM\", \"seed\": 3}");
        String second = generate("{\"strategy\": \"RANDOM\", \"seed\": 3}");

        assertThat(JsonPath.<List<String>>read(second, "$.groups[*].members[*].studentNumber"))
                .isEqualTo(JsonPath.<List<String>>read(first, "$.groups[*].members[*].studentNumber"));
    }

    @Test
    void anUnknownStrategyIsRejected() throws Exception {
        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content("{\"strategy\": \"MAGIC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown strategy MAGIC. Use one of [BALANCED, RANDOM]"));
    }

    @Test
    void aProjectWithFewerThanTwoStudentsCannotBeGrouped() throws Exception {
        project.replaceStudents(List.of(students.getFirst()));

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("At least 2 eligible students are needed to form groups, but there are 1"));
    }

    @Test
    @WithMockUser(username = "rui", roles = "TEACHER")
    void anotherTeacherCannotGenerateOrSeeTheGroups() throws Exception {
        accountRepository.save(new UserAccount("rui", "Prof. Rui", "hash", Role.TEACHER));

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(url())).andExpect(status().isNotFound());
    }

    @Test
    void theStrategiesAreListed() throws Exception {
        mockMvc.perform(get("/api/grouping-strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BALANCED"))
                .andExpect(jsonPath("$[1].code").value("RANDOM"));
    }
}
