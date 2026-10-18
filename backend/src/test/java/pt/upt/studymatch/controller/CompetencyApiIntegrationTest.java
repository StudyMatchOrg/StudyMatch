package pt.upt.studymatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.CourseUnitCompetency;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitCompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompetencyApiIntegrationTest {

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

    private CourseUnit aed;

    @BeforeEach
    void createData() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        CourseUnit ip = courseUnitRepository.save(new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(lei)));
        aed = courseUnitRepository.save(new CourseUnit("AED", "Algoritmos e Estruturas de Dados", 7.5, 2, 1, Set.of(lei)));
        Competency programming = competencyRepository.save(new Competency("PROG", "Programming", "Writing code."));
        Competency algorithms = competencyRepository.save(new Competency("ALGO", "Algorithms", "Algorithms."));
        linkRepository.save(new CourseUnitCompetency(ip, programming, 1.0));
        linkRepository.save(new CourseUnitCompetency(aed, programming, 0.5));
        linkRepository.save(new CourseUnitCompetency(aed, algorithms, 1.0));
    }

    @Test
    void competenciesAreListedByName() throws Exception {
        mockMvc.perform(get("/api/competencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Algorithms"))
                .andExpect(jsonPath("$[1].name").value("Programming"));
    }

    @Test
    void allWeightsAreListedInCurricularOrder() throws Exception {
        mockMvc.perform(get("/api/course-unit-competencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].courseUnitCode").value("IP"))
                .andExpect(jsonPath("$[1].courseUnitCode").value("AED"))
                .andExpect(jsonPath("$[1].competency.code").value("ALGO"))
                .andExpect(jsonPath("$[1].weight").value(1.0));
    }

    @Test
    void weightsOfOneCourseUnitAreListedByWeight() throws Exception {
        mockMvc.perform(get("/api/course-unit-competencies").param("courseUnitId", aed.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].competency.code").value("ALGO"))
                .andExpect(jsonPath("$[1].weight").value(0.5));
    }

    @Test
    void unknownCourseUnitReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/course-unit-competencies").param("courseUnitId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course unit 9999 does not exist"));
    }
}
