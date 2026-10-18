package pt.upt.studymatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Not {@code @Transactional}: the import saves each line in its own transaction, so the test
 * data must be committed first and is removed afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ImportApiIntegrationTest {

    private static final String URL = "/api/imports/academic-records";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseUnitRepository courseUnitRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicRecordRepository recordRepository;

    private Student carla;

    @BeforeEach
    void createData() {
        Degree lig = degreeRepository.save(new Degree("LIG", "Informática de Gestão"));
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lig, lei)));
        courseUnitRepository.save(new CourseUnit("RC", "Redes de Computadores", 6, 2, 2, Set.of(lei)));
        carla = studentRepository.save(new Student("53015", "Carla Mendes", "53015@alunos.upt.pt", lig, 2026));
    }

    @AfterEach
    void removeData() {
        recordRepository.deleteAll();
        studentRepository.deleteAll();
        courseUnitRepository.deleteAll();
        degreeRepository.deleteAll();
    }

    private static MockMultipartFile csv(String content) {
        return new MockMultipartFile("file", "records.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void validLinesAreImportedAndInvalidLinesAreReported() throws Exception {
        String content = """
                studentNumber,courseUnitCode,academicYear,semester,status,grade
                53015,IP,2026/2027,1,PASSED,14
                53015,RC,2026/2027,2,PASSED,12
                53015,IP,2025/2026,1,PASSED,23
                99999,IP,2026/2027,1,PASSED,15
                53015,IP,2026/2027,1,FAILED,8
                53015,IP,2025/2026,1,APROVADO,12
                """;

        mockMvc.perform(multipart(URL).file(csv(content)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLines").value(6))
                .andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.rejected.length()").value(5))
                .andExpect(jsonPath("$.rejected[0].line").value(3))
                .andExpect(jsonPath("$.rejected[0].message").value("Course unit RC is not part of the degree LIG"))
                .andExpect(jsonPath("$.rejected[1].message").value("Grade must be between 0 and 20, but was 23"))
                .andExpect(jsonPath("$.rejected[2].message").value("Unknown student number 99999"))
                .andExpect(jsonPath("$.rejected[3].message").value("There is already a record for IP in 2026/2027, semester 1"))
                .andExpect(jsonPath("$.rejected[4].message").value("Unknown status 'APROVADO': use PASSED, FAILED or ENROLLED"));

        assertThat(recordRepository.findByStudentInOrder(carla.getId())).hasSize(1);
    }

    @Test
    void emptyFileIsRejected() throws Exception {
        mockMvc.perform(multipart(URL).file(csv("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The file is empty"));
    }

    @Test
    void fileWithoutTheRequiredColumnsIsRejected() throws Exception {
        mockMvc.perform(multipart(URL).file(csv("number,grade\n53015,14\n")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Missing columns")));
    }

    @Test
    void requestWithoutAFileIsRejected() throws Exception {
        mockMvc.perform(multipart(URL))
                .andExpect(status().isBadRequest());
    }
}
