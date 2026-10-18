package pt.upt.studymatch.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcademicRecordApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student ana;
    private CourseUnit ip;
    private CourseUnit poo;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        poo = courseUnitRepository.save(new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(lei)));
        ana = studentRepository.save(new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", lei, 2025));
    }

    private String recordsUrl() {
        return "/api/students/" + ana.getId() + "/academic-records";
    }

    private String body(CourseUnit unit, String status, Integer grade) {
        return """
                {"courseUnitId": %d, "academicYear": "2025/2026", "semester": %d, "status": "%s", "grade": %s}
                """.formatted(unit.getId(), unit.getSemester(), status, grade);
    }

    private String add(CourseUnit unit, String status, Integer grade) throws Exception {
        String response = mockMvc.perform(post(recordsUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(unit, status, grade)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id").toString();
    }

    @Test
    void recordsAreListedInChronologicalOrder() throws Exception {
        add(poo, "PASSED", 15);
        add(ip, "PASSED", 17);

        mockMvc.perform(get(recordsUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].courseUnit.code").value("IP"))
                .andExpect(jsonPath("$[1].courseUnit.code").value("POO"))
                .andExpect(jsonPath("$[1].courseUnit.ects").value(7.5));
    }

    @Test
    void invalidGradeReturnsBadRequestWithTheReason() throws Exception {
        mockMvc.perform(post(recordsUrl()).contentType(MediaType.APPLICATION_JSON).content(body(ip, "PASSED", 21)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Grade must be between 0 and 20, but was 21"));
    }

    @Test
    void sameAttemptTwiceReturnsConflict() throws Exception {
        add(ip, "FAILED", 8);

        mockMvc.perform(post(recordsUrl()).contentType(MediaType.APPLICATION_JSON).content(body(ip, "PASSED", 12)))
                .andExpect(status().isConflict());
    }

    @Test
    void gradeCanBeChangedAndRecordDeleted() throws Exception {
        String id = add(ip, "ENROLLED", null);
        String change = """
                {"academicYear": "2025/2026", "semester": 1, "status": "PASSED", "grade": 14}
                """;

        mockMvc.perform(put("/api/academic-records/" + id).contentType(MediaType.APPLICATION_JSON).content(change))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value(14));
        mockMvc.perform(delete("/api/academic-records/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(recordsUrl()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recordsOfUnknownStudentReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/students/9999/academic-records"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student 9999 does not exist"));
    }
}
